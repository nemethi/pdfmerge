package nemethi.pdfmerge.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;

public class OutputStreamSupplier {

    public OutputStream getFileStream(Path path) throws FileNotFoundException {
        return new FileOutputStream(path.toFile());
    }
}
