package core;

import java.awt.image.BufferedImage;
import java.io.File;
import util.InputScanner;

public abstract class CompressQuadtree {
    // Shared attributes
    protected final InputScanner input;
    protected File imageFileInput;
    protected BufferedImage imageInput;
    protected double threshold;
    protected int minBlockSize;
    protected double targetCompressionRate;
    protected File imageFileOutput;

     // // Output
    // private long executionTime;
    // private int originalSize;
    // private int compressedSize;
    // private double actualCompressionRatio;
    // private int treeDepth;
    // private int treeNodeCount;

    public CompressQuadtree(File imageFileInput, BufferedImage imageInput, InputScanner input) {
        this.imageFileInput = imageFileInput;
        this.imageInput = imageInput;
        this.input = input;
        threshold = this.input.scan("[INPUT] Ambang batas (threshold): ", Double::parseDouble);
        minBlockSize = this.input.scan("[INPUT] Ukuran blok minimum: ", Integer::parseInt);
        targetCompressionRate = this.input.scan("[INPUT] Target persentase kompresi (0 untuk nonaktif): ", Double::parseDouble);
        String outputPath = this.input.scanString("[INPUT] Alamat absolut gambar hasil kompresi: ");
        imageFileOutput = new File(outputPath);
    }

    public abstract void compress();

    // Optional helper methods
    protected void logStart() {
        System.out.println("Starting compression for: " + imageFileInput.getName());
    }

    protected void logFinish() {
        System.out.println("Compression finished. Output saved to: " + imageFileOutput.getPath());
    }

   
}