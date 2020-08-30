package nemethi.pdfmerge.util;

import java.nio.file.Files;
import java.nio.file.Path;

public class FileChecker {

    public boolean exists(Path path) {
        return Files.exists(path);
    }

    public boolean notExists(Path path) {
        return Files.notExists(path);
    }

    public boolean isDirectory(Path path) {
        return Files.isDirectory(path);
    }
}
