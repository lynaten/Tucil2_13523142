package core;

import java.awt.image.BufferedImage;
import java.io.File;
import util.InputScanner;

public class CompressVariance extends CompressQuadtree {
    public CompressVariance(File imageFileInput, BufferedImage imageInput, InputScanner input) {
        super(imageFileInput, imageInput, input);
    }

    @Override
    public void compress() {
        logStart();
        QuadBuilder.threshold = this.threshold;
        QuadBuilder.minBlockSize = this.minBlockSize;
        QuadNode tree = QuadBuilder.buildTree(
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
        System.out.println("====== HASIL KOMPRESI METHODE VARIANCE ======");
        super.output();
    }

    private static class QuadNode {
        public int depth = 1;
        public int totalNodes = 1;
        public long rSum, gSum, bSum;
        public long rSumSq, gSumSq, bSumSq;
        public QuadNode[] children = new QuadNode[4];
    }

    private static class QuadBuilder {
        public static double threshold;
        public static int minBlockSize;

        public static QuadNode buildTree(BufferedImage image, BufferedImage result, int x, int y, int width, int height) {
            QuadNode node = new QuadNode();
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

                result.setRGB(x, y, (red << 16) | (green << 8) | blue);
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
                    node.rSum += child.rSum;
                    node.gSum += child.gSum;
                    node.bSum += child.bSum;

                    node.rSumSq += child.rSumSq;
                    node.gSumSq += child.gSumSq;
                    node.bSumSq += child.bSumSq;

                    maxChildDepth = Math.max(maxChildDepth, child.depth);
                    total += child.totalNodes;
                }
            }

            int pixelCount = width * height;
            double variance = calculateVariance(node, pixelCount);

            if (variance <= threshold || pixelCount <= minBlockSize) {
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

        private static double calculateVariance(QuadNode node, int pixelCount) {
            if (pixelCount == 0) return 0;
            double rVar = (double) node.rSumSq / pixelCount - Math.pow((double) node.rSum / pixelCount, 2);
            double gVar = (double) node.gSumSq / pixelCount - Math.pow((double) node.gSum / pixelCount, 2);
            double bVar = (double) node.bSumSq / pixelCount - Math.pow((double) node.bSum / pixelCount, 2);
            return (rVar + gVar + bVar) / 3;
        }
    }
}
