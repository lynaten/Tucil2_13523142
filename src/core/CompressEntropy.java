package core;

import java.awt.image.BufferedImage;
import java.io.File;
import util.InputScanner;

public class CompressEntropy extends CompressQuadtree {
    public CompressEntropy(File imageFileInput, BufferedImage imageInput, InputScanner input) {
        super(imageFileInput, imageInput, input);
    }

    @Override
    public void compress() {
        logStart();
        QuadBuilder builder = new EntropyQuadBuilder(this.threshold, this.minBlockSize);
        QuadNode tree = builder.buildTree(
            this.imageInput, imageOutput, 0, 0,
            this.imageInput.getWidth(), this.imageInput.getHeight()
        );
        this.treeNodeCount = tree.totalNodes;
        this.treeDepth =  tree.depth;
        logFinish();
    }

    @Override
    public void output(){
        System.out.println();
        System.out.println("====== HASIL KOMPRESI METODE ENTROPY ======");
        super.output();
    }

    private static class QuadNodeEntropy extends QuadNode {
        public long rSum, gSum, bSum;
        public long rSumSq, gSumSq, bSumSq;
        public int[] rListCount = new int[256];
        public int[] gListCount = new int[256];
        public int[] bListCount = new int[256];

        public QuadNodeEntropy() {
            super();
        }
    }

    private class EntropyQuadBuilder extends QuadBuilder {
        public EntropyQuadBuilder(double threshold, int minBlockSize) {
            super(threshold, minBlockSize);
        }

        @Override
        public QuadNode buildTree(BufferedImage image, BufferedImage result, int x, int y, int width, int height) {
            QuadNodeEntropy node = new QuadNodeEntropy();

            if (width == 1 && height == 1) {
                int rgb = image.getRGB(x, y);
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;

                node.rSum = red;
                node.gSum = green;
                node.bSum = blue;

                node.rSumSq = (long) red * red;
                node.gSumSq = (long) green * green;
                node.bSumSq = (long) blue * blue;

                node.rListCount[red]++;
                node.gListCount[green]++;
                node.bListCount[blue]++;

                result.setRGB(x, y, rgb);
                return node;
            }

            int w1 = width / 2 + width % 2;
            int w2 = width / 2;
            int h1 = height / 2 + height % 2;
            int h2 = height / 2;

            node.children[0] = buildTree(image, result, x, y, w1, h1);
            if (w2 > 0) node.children[1] = buildTree(image, result, x + w1, y, w2, h1);
            if (h2 > 0) node.children[2] = buildTree(image, result, x, y + h1, w1, h2);
            if (w2 > 0 && h2 > 0) node.children[3] = buildTree(image, result, x + w1, y + h1, w2, h2);

            int maxChildDepth = 0;
            int total = 1;

            for (QuadNode child : node.children) {
                if (child != null) {
                    QuadNodeEntropy childNode = (QuadNodeEntropy) child;

                    node.rSum += childNode.rSum;
                    node.gSum += childNode.gSum;
                    node.bSum += childNode.bSum;

                    node.rSumSq += childNode.rSumSq;
                    node.gSumSq += childNode.gSumSq;
                    node.bSumSq += childNode.bSumSq;

                    for (int i = 0; i < 256; i++) {
                        node.rListCount[i] += childNode.rListCount[i];
                        node.gListCount[i] += childNode.gListCount[i];
                        node.bListCount[i] += childNode.bListCount[i];
                    }

                    maxChildDepth = Math.max(maxChildDepth, child.depth);
                    total += child.totalNodes;
                }
            }

            int pixelCount = width * height;
            double entropy = calculateEntropy(node, pixelCount);

            if (entropy <= threshold || pixelCount <= minBlockSize) {
                node.children = new QuadNode[4];
                node.depth = 1;
                node.totalNodes = 1;

                int avgR = (int) Math.round((double) node.rSum / pixelCount);
                int avgG = (int) Math.round((double) node.gSum / pixelCount);
                int avgB = (int) Math.round((double) node.bSum / pixelCount);
                int rgb = (avgR << 16) | (avgG << 8) | avgB;

                for (int dy = 0; dy < height; dy++) {
                    for (int dx = 0; dx < width; dx++) {
                        if (y + dy < result.getHeight() && x + dx < result.getWidth()) {
                            result.setRGB(x + dx, y + dy, rgb);
                        }
                    }
                }
            } else {
                node.depth = 1 + maxChildDepth;
                node.totalNodes = total;
            }

            return node;
        }

        private double calculateEntropy(QuadNodeEntropy node, int pixelCount) {
            if (pixelCount == 0) return 0;

            double rEntropy = entropyFromHistogram(node.rListCount, pixelCount);
            double gEntropy = entropyFromHistogram(node.gListCount, pixelCount);
            double bEntropy = entropyFromHistogram(node.bListCount, pixelCount);

            return (rEntropy + gEntropy + bEntropy) / 3.0;
        }

        private double entropyFromHistogram(int[] histogram, int totalPixels) {
            double entropy = 0;
            for (int count : histogram) {
                if (count == 0) continue;
                double p = (double) count / totalPixels;
                entropy += p * (Math.log(p) / Math.log(2));
            }
            return -entropy;
        }
    }
}
