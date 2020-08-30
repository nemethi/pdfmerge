package nemethi.pdfmerge;

import nemethi.pdfmerge.util.FileChecker;
import nemethi.pdfmerge.util.OutputStreamSupplier;
import nemethi.pdfmerge.util.PathToStreamConverter;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.util.List;

public class PdfMerger {

    private final PDFMergerUtility mergerUtility;
    private PathToStreamConverter converter;
    private FileChecker fileChecker;
    private OutputStreamSupplier streamSupplier;

    public PdfMerger(PDFMergerUtility mergerUtility) {
        this.mergerUtility = mergerUtility;
        converter = new PathToStreamConverter();
        fileChecker = new FileChecker();
        streamSupplier = new OutputStreamSupplier();
    }

    public void merge(List<Path> inputPaths, Path outputPath) throws IOException {
        if (fileChecker.exists(outputPath)) {
            throw new FileAlreadyExistsException("The output file already exists.");
        }
        doMerge(inputPaths, outputPath);
    }

    public void forceMerge(List<Path> inputPaths, Path outputPath) throws IOException {
        doMerge(inputPaths, outputPath);
    }

    private void doMerge(List<Path> inputPaths, Path outputPath) throws IOException {
        try (OutputStream outputStream = streamSupplier.getFileStream(outputPath)) {
            mergerUtility.addSources(converter.convertPathsToStreams(inputPaths));
            mergerUtility.setDestinationStream(outputStream);
            mergerUtility.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
        }
    }

    public void setConverter(PathToStreamConverter converter) {
        this.converter = converter;
    }

    public void setFileChecker(FileChecker fileChecker) {
        this.fileChecker = fileChecker;
    }

    public void setStreamSupplier(OutputStreamSupplier streamSupplier) {
        this.streamSupplier = streamSupplier;
    }
}
