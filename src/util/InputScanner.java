package util;
import java.io.File;
import java.util.Scanner;
import java.util.function.Function;

public class InputScanner {
    private final Scanner scanner;

    public InputScanner() {
        this.scanner = new Scanner(System.in);
    }

    public <T> T scan(String prompt, Function<String, T> parser) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();
            try {
                return parser.apply(input);
            } catch (Exception e) {
                System.out.println("Input tidak valid: " + e.getMessage());
            }
        }
    }
    
    public File scanFile(String prompt) {
        return scan(prompt, path -> {
            File file = new File(path);
            if (!file.exists()) throw new IllegalArgumentException("File tidak ditemukan.");
            return file;
        });
    }


    public void close() {
        scanner.close();
    }
}