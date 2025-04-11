package core;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import util.InputScanner;

public abstract class CompressQuadtree {
    protected final InputScanner input;
    protected File imageFileInput;
    protected BufferedImage imageInput;
    protected double threshold;
    protected int minBlockSize;
    protected File imageFileOutput;
    // protected double targetCompressionRate;
    
    protected BufferedImage imageOutput;
    protected long executionTime;
    protected int originalSize;
    protected int compressedSize;
    protected double actualCompressionRatio;
    protected int treeDepth;
    protected int treeNodeCount;

    private long startTime;

    public CompressQuadtree(File imageFileInput, BufferedImage imageInput, InputScanner input) {
        this.imageFileInput = imageFileInput;
        this.imageInput = imageInput;
        this.input = input;
        threshold = this.input.scan("[INPUT] Ambang batas (threshold): ", Double::parseDouble);
        minBlockSize = this.input.scan("[INPUT] Ukuran blok minimum: ", Integer::parseInt);
        // targetCompressionRate = this.input.scan("[INPUT] Target persentase kompresi (0 untuk nonaktif): ", Double::parseDouble);
        String outputPath = this.input.scanString("[INPUT] Alamat absolut gambar hasil kompresi: ");
        imageFileOutput = new File(outputPath);

        this.imageOutput = new BufferedImage(
            this.imageInput.getWidth(), this.imageInput.getHeight(), BufferedImage.TYPE_INT_RGB
        );
    }

    public abstract void compress();

    protected void logStart() {
        System.out.println("Starting compression for: " + imageFileInput.getName());
        startTime = System.nanoTime();
    }

    protected void logFinish() {
        long endTime = System.nanoTime();
        executionTime = endTime - startTime;
    }

    private void updateFileSizes() {
        originalSize = (int) imageFileInput.length();
        File outputFile = new File(imageFileOutput.getAbsolutePath());
        if (outputFile.exists()) {
            compressedSize = (int) outputFile.length();
        }
    }
    
    public void saveImageOutput() {
        try {
            String formatName = "png";
            ImageIO.write(imageOutput, formatName, imageFileOutput);
            updateFileSizes();
        } catch (IOException e) {
            System.out.println("Gagal menyimpan gambar: " + e.getMessage());
        }
    }

    public void output() {
        System.out.printf("[OUTPUT] Waktu eksekusi           : %.2f ms\n", executionTime / 1_000_000.0);
        System.out.printf("[OUTPUT] Ukuran gambar sebelum    : %.2f KB (%d bytes)\n", originalSize / 1024.0, originalSize);
        System.out.printf("[OUTPUT] Ukuran gambar setelah    : %.2f KB (%d bytes)\n", compressedSize / 1024.0, compressedSize);
        if (originalSize > 0) {
            actualCompressionRatio = (1 - ((double) compressedSize / originalSize)) * 100;
            System.out.printf("[OUTPUT] Persentase kompresi      : %.2f%%\n", actualCompressionRatio);
        } else {
            System.out.println("[OUTPUT] Persentase kompresi      : N/A (ukuran asli 0)");
        }
        System.out.printf("[OUTPUT] Kedalaman pohon          : %d\n", treeDepth);
        System.out.printf("[OUTPUT] Banyak simpul            : %d\n", treeNodeCount);
        System.out.printf("[OUTPUT] Gambar output disimpan di: %s\n", imageFileOutput.getAbsolutePath());

    }

    protected abstract static class QuadNode {
        public int depth = 1;
        public int totalNodes = 1;
        public QuadNode[] children = new QuadNode[4];

        public QuadNode() {
        }
    }

    protected abstract class QuadBuilder {
        protected double threshold;
        protected int minBlockSize;
    
        public QuadBuilder(double threshold, int minBlockSize) {
            this.threshold = threshold;
            this.minBlockSize = minBlockSize;
        }

        public abstract QuadNode buildTree(BufferedImage image, BufferedImage result, int x, int y, int width, int height);
    }

}
