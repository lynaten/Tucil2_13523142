package core;

import java.awt.image.BufferedImage;
import java.io.File;
import util.InputScanner;

public class CompressMPD extends CompressQuadtree {
    public CompressMPD(File imageFileInput, BufferedImage imageInput, InputScanner input) {
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
        System.out.println("====== HASIL KOMPRESI METODE MPD ======");
        super.output();
    }

    private static class QuadNode {
        public int depth = 1;
        public int totalNodes = 1;
        int minR = 255, maxR = 0;
        int minG = 255, maxG = 0;
        int minB = 255, maxB = 0;
        long rSum, gSum, bSum;
        QuadNode[] children = new QuadNode[4];
    }

    private static class QuadBuilder {
        public static double threshold;
        public static int minBlockSize;

        public static QuadNode buildTree(BufferedImage image, BufferedImage result, int x, int y, int width, int height) {
            QuadNode node = new QuadNode();

            if (width == 1 && height == 1) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                node.rSum = r;
                node.gSum = g;
                node.bSum = b;

                node.minR = node.maxR = r;
                node.minG = node.maxG = g;
                node.minB = node.maxB = b;

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
                    node.rSum += child.rSum;
                    node.gSum += child.gSum;
                    node.bSum += child.bSum;

                    node.minR = Math.min(node.minR, child.minR);
                    node.maxR = Math.max(node.maxR, child.maxR);
                    node.minG = Math.min(node.minG, child.minG);
                    node.maxG = Math.max(node.maxG, child.maxG);
                    node.minB = Math.min(node.minB, child.minB);
                    node.maxB = Math.max(node.maxB, child.maxB);

                    maxChildDepth = Math.max(maxChildDepth, child.depth);
                    total += child.totalNodes;
                }
            }

            int pixelCount = width * height;
            double diff = calculateMaxDiff(node);

            if (diff <= threshold || pixelCount <= minBlockSize) {
                node.children = new QuadNode[4];

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

        private static double calculateMaxDiff(QuadNode node) {
            int dR = node.maxR - node.minR;
            int dG = node.maxG - node.minG;
            int dB = node.maxB - node.minB;
            return (dR + dG + dB) / 3.0;
        }
    }
}
