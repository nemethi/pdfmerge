package nemethi.pdfmerge.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PathToStreamConverter {

    public List<InputStream> convertPathsToStreams(List<Path> paths) throws FileNotFoundException {
        List<InputStream> streams = new ArrayList<>();
        for (Path path : paths) {
            streams.add(new FileInputStream(path.toFile()));
        }
        return streams;
    }
}
