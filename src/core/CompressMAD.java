package core;

import java.awt.image.BufferedImage;
import java.io.File;
import util.InputScanner;

public class CompressMAD extends CompressQuadtree {
    public CompressMAD(File imageFileInput, BufferedImage imageInput, InputScanner input) {
        super(imageFileInput, imageInput, input);
    }

    @Override
    public void compress() {
        logStart();
        QuadBuilder builder = new MADQuadBuilder(this.threshold, this.minBlockSize);
        QuadNode tree = builder.buildTree(
            this.imageInput, imageOutput, 0, 0,
            this.imageInput.getWidth(), this.imageInput.getHeight()
        );
        this.treeNodeCount = tree.totalNodes;
        this.treeDepth = tree.depth;
        logFinish();
    }

    @Override
    public void output() {
        System.out.println();
        System.out.println("====== HASIL KOMPRESI METODE MAD ======");
        super.output();
    }

    private static class QuadNodeMAD extends QuadNode {
        public long rSum, gSum, bSum;
        public int[] rPixels, gPixels, bPixels;

        public QuadNodeMAD() {
            super();
        }
    }

    private class MADQuadBuilder extends QuadBuilder {
        public MADQuadBuilder(double threshold, int minBlockSize) {
            super(threshold, minBlockSize);
        }

        @Override
        public QuadNode buildTree(BufferedImage image, BufferedImage result, int x, int y, int width, int height) {
            QuadNodeMAD node = new QuadNodeMAD();
            int pixelCount = width * height;

            if (width == 1 && height == 1) {
                int rgb = image.getRGB(x, y);
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;

                node.rSum = red;
                node.gSum = green;
                node.bSum = blue;

                node.rPixels = new int[]{red};
                node.gPixels = new int[]{green};
                node.bPixels = new int[]{blue};

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

            node.rPixels = new int[pixelCount];
            node.gPixels = new int[pixelCount];
            node.bPixels = new int[pixelCount];

            int i = 0;
            int maxDepth = 0;
            int totalNodes = 1;

            for (QuadNode child : node.children) {
                if (child != null) {
                    QuadNodeMAD childNode = (QuadNodeMAD) child;

                    node.rSum += childNode.rSum;
                    node.gSum += childNode.gSum;
                    node.bSum += childNode.bSum;

                    System.arraycopy(childNode.rPixels, 0, node.rPixels, i, childNode.rPixels.length);
                    System.arraycopy(childNode.gPixels, 0, node.gPixels, i, childNode.gPixels.length);
                    System.arraycopy(childNode.bPixels, 0, node.bPixels, i, childNode.bPixels.length);
                    i += childNode.rPixels.length;

                    maxDepth = Math.max(maxDepth, child.depth);
                    totalNodes += child.totalNodes;
                }
            }

            double mad = calculateMAD(node, pixelCount);

            if (mad <= threshold || pixelCount <= minBlockSize) {
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
                node.depth = 1 + maxDepth;
                node.totalNodes = totalNodes;
            }

            return node;
        }

        private double calculateMAD(QuadNodeMAD node, int pixelCount) {
            if (pixelCount == 0) return 0;

            double meanR = (double) node.rSum / pixelCount;
            double meanG = (double) node.gSum / pixelCount;
            double meanB = (double) node.bSum / pixelCount;

            double totalDevR = 0, totalDevG = 0, totalDevB = 0;
            for (int i = 0; i < pixelCount; i++) {
                totalDevR += Math.abs(node.rPixels[i] - meanR);
                totalDevG += Math.abs(node.gPixels[i] - meanG);
                totalDevB += Math.abs(node.bPixels[i] - meanB);
            }

            double madR = totalDevR / pixelCount;
            double madG = totalDevG / pixelCount;
            double madB = totalDevB / pixelCount;

            return (madR + madG + madB) / 3.0;
        }
    }
}
