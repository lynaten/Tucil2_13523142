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
        // TODO: implement MAD compression logic
        logFinish();
    }
}