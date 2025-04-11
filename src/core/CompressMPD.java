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
        QuadBuilder builder = new MPDQuadBuilder(this.threshold, this.minBlockSize);
        QuadNode tree = builder.buildTree(
            this.imageInput, imageOutput, 0, 0,
            this.imageInput.getWidth(), this.imageInput.getHeight()
        );
        this.treeNodeCount = tree.totalNodes;
        this.treeDepth =  tree.depth;
        logFinish();
    }

    @Override
    public void output() {
        System.out.println();
        System.out.println("====== HASIL KOMPRESI METODE MPD ======");
        super.output();
    }

    private static class QuadNodeMPD extends QuadNode {
        int minR = 255, maxR = 0;
        int minG = 255, maxG = 0;
        int minB = 255, maxB = 0;
        long rSum, gSum, bSum;
        public QuadNodeMPD() {
            super();
        }
    }

    private class MPDQuadBuilder extends QuadBuilder {
        public MPDQuadBuilder(double threshold, int minBlockSize) {
            super(threshold, minBlockSize);
        }

        @Override
        public QuadNode buildTree(BufferedImage image, BufferedImage result, int x, int y, int width, int height) {
            QuadNodeMPD node = new QuadNodeMPD();

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
                    QuadNodeMPD childNode = (QuadNodeMPD) child;

                    node.rSum += childNode.rSum;
                    node.gSum += childNode.gSum;
                    node.bSum += childNode.bSum;

                    node.minR = Math.min(node.minR, childNode.minR);
                    node.maxR = Math.max(node.maxR, childNode.maxR);
                    node.minG = Math.min(node.minG, childNode.minG);
                    node.maxG = Math.max(node.maxG, childNode.maxG);
                    node.minB = Math.min(node.minB, childNode.minB);
                    node.maxB = Math.max(node.maxB, childNode.maxB);

                    maxChildDepth = Math.max(maxChildDepth, child.depth);
                    total += child.totalNodes;
                }
            }

            int pixelCount = width * height;
            double diff = calculateMaxDiff(node);

            if (diff <= threshold || pixelCount <= minBlockSize) {
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

        private double calculateMaxDiff(QuadNodeMPD node) {
            int dR = node.maxR - node.minR;
            int dG = node.maxG - node.minG;
            int dB = node.maxB - node.minB;
            return (dR + dG + dB) / 3.0;
        }
    }
}