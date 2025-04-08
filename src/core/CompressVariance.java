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
        // TODO: implement compression logic using variance as error measurement
        logFinish();
    }
}