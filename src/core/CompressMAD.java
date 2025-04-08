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
        // TODO: implement MAD compression logic
        logFinish();
    }
}