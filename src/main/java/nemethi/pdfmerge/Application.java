package nemethi.pdfmerge;

import nemethi.pdfmerge.cli.ExceptionHandler;
import nemethi.pdfmerge.cli.VersionProvider;
import nemethi.pdfmerge.util.FileChecker;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "pdfmerge", mixinStandardHelpOptions = true, versionProvider = VersionProvider.class)
public class Application implements Callable<Integer> {

    private final PdfMerger pdfMerger;
    private Path outputFile;
    private List<Path> inputFiles;
    private FileChecker fileChecker;
    private CommandSpec spec;
    private boolean isForced;

    public Application(PdfMerger pdfMerger) {
        this.pdfMerger = pdfMerger;
        this.fileChecker = new FileChecker();
    }

    public static void main(String[] args) {
        Application application = new Application(new PdfMerger(new PDFMergerUtility()));
        ExceptionHandler exceptionHandler = new ExceptionHandler();
        int exitCode = new CommandLine(application)
                .setParameterExceptionHandler(exceptionHandler)
                .setExecutionExceptionHandler(exceptionHandler)
                .execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        if (isForced) {
            pdfMerger.forceMerge(inputFiles, outputFile);
        } else {
            pdfMerger.merge(inputFiles, outputFile);
        }
        return 0;
    }

    @Option(names = {"-o", "--output"}, paramLabel = "OUTFILE", required = true, description = "Path to the output file.")
    public void setOutputFile(Path outputFile) {
        if (fileChecker.isDirectory(outputFile)) {
            throw new ParameterException(spec.commandLine(), "Invalid path: OUTFILE must point to a file.");
        }
        this.outputFile = outputFile;
    }

    @Parameters(paramLabel = "FILE", arity = "2..*", description = "Path to the files to be merged.")
    public void setInputFiles(List<Path> inputFiles) {
        for (Path inputFile : inputFiles) {
            if (fileChecker.notExists(inputFile)) {
                throw new ParameterException(spec.commandLine(), String.format("Invalid path: %s does not exist.", inputFile));
            } else if (fileChecker.isDirectory(inputFile)) {
                throw new ParameterException(spec.commandLine(), "Invalid path: FILE must point to a file.");
            }
        }
        this.inputFiles = inputFiles;
    }

    @Option(names = {"-f", "--force"}, description = "Overwrite OUTFILE.")
    public void setForced(boolean forced) {
        isForced = forced;
    }

    @Spec
    public void setSpec(CommandSpec spec) {
        this.spec = spec;
    }

    public Path getOutputFile() {
        return outputFile;
    }

    public List<Path> getInputFiles() {
        return inputFiles;
    }

    public void setFileChecker(FileChecker fileChecker) {
        this.fileChecker = fileChecker;
    }
}
