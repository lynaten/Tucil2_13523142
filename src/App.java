import core.CompressEntropy;
import core.CompressMAD;
import core.CompressMPD;
import core.CompressQuadtree;
import core.CompressVariance;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import util.InputScanner;

public class App {
    private final InputScanner input;
    private File imageFileInput;
    private BufferedImage imageInput;

    public App() {
        input = new InputScanner();
    }

    public void selectImageFile() {
        while (true) {
            imageFileInput = input.scanFile("[INPUT] Alamat absolut gambar yang akan dikompresi: ");
            try {
                imageInput = ImageIO.read(imageFileInput);
                if (imageInput != null) {
                    System.out.println("Gambar berhasil dimuat: " + imageInput.getWidth() + "x" + imageInput.getHeight());
                    break;
                } else {
                    System.out.println("Gagal membaca gambar. Format mungkin tidak didukung.");
                }
            } catch (IOException e) {
                System.out.println("Terjadi kesalahan saat membaca gambar: " + e.getMessage());
            }
        }
    }

    public void printMethods() {
        System.out.println("Error Measurement Methods:");
        System.out.println("1. Variance");
        System.out.println("2. Mean Absolute Deviation (MAD)");
        System.out.println("3. Max Pixel Difference");
        System.out.println("4. Entropy");
    }

    public CompressQuadtree chooseCompressor() {
        printMethods();
        int method = input.scan("[INPUT] Pilih metode (1-4): ", choice -> {
            int val = Integer.parseInt(choice);
            if (val < 1 || val > 4) throw new IllegalArgumentException("Harus antara 1 hingga 4.");
            return val;
        });
        return switch (method) {
            case 1 -> new CompressVariance(imageFileInput, imageInput,input);
            case 2 -> new CompressMAD(imageFileInput, imageInput,input);
            case 3 -> new CompressMPD(imageFileInput, imageInput,input);
            case 4 -> new CompressEntropy(imageFileInput, imageInput,input);
            default -> throw new IllegalStateException("Unexpected method: " + method);
        };
    }

    public void close() {
        input.close();
    }

    public static void main(String[] args) {
        App app = new App();
        app.selectImageFile();
        CompressQuadtree compressor = app.chooseCompressor();
        compressor.compress();
        app.close();
    }
}