package nemethi.pdfmerge.integration;

import nemethi.pdfmerge.Application;
import nemethi.pdfmerge.PdfMerger;
import nemethi.pdfmerge.cli.ExceptionHandler;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Version options are intentionally not tested.
 * From the IDE it cannot resolve the version; from Maven it can. (because the JAR is already built)
 * Also the version fetching is dynamic, and I do not want to adjust this test every time the version changes.
 * Dynamic version fetching was implemented to be able to change the version from one place: the POM.
 */
public class CommandLineIT {

    private static final String NO_INPUT_FILE_ERROR_MESSAGE = "pdfmerge: positional parameter at index 0..* (FILE) requires at least 2 values, but none were specified.\n";
    private static final String INVALID_OUTPUT_FILE_ERROR_MESSAGE = "pdfmerge: Invalid path: OUTFILE must point to a file.\n";
    private static final String INVALID_INPUT_FILE_ERROR_MESSAGE = "pdfmerge: Invalid path: FILE must point to a file.\n";
    private static final String NOT_EXISTING_FILE_ERROR_MESSAGE_FORMAT = "pdfmerge: Invalid path: %s does not exist.%n";
    private static final String NOT_ENOUGH_INPUT_FILES_ERROR_MESSAGE_FORMAT = "pdfmerge: positional parameter at index 0..* (FILE) requires at least 2 values, but only 1 were specified: [%s]%n";

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private CommandLine cli;
    private StringWriter stdout;
    private StringWriter stderr;

    @Before
    public void setUp() {
        stdout = new StringWriter();
        stderr = new StringWriter();
        Application application = new Application(new PdfMerger(new PDFMergerUtility()));
        ExceptionHandler exceptionHandler = new ExceptionHandler();
        cli = new CommandLine(application)
                .setParameterExceptionHandler(exceptionHandler)
                .setExecutionExceptionHandler(exceptionHandler)
                .setOut(new PrintWriter(stdout))
                .setErr(new PrintWriter(stderr));

    }

    @Test
    public void outputShortOptionInvalidFile() throws IOException {
        // given
        String folder = temp.newFolder().getCanonicalPath();

        // when
        int exitCode = cli.execute("-o", folder);

        // then
        assertThat(exitCode).isEqualTo(2);
        assertThat(stderr).hasToString(INVALID_OUTPUT_FILE_ERROR_MESSAGE);
        assertHelpMessage();
    }

    @Test
    public void outputLongOptionInvalidFile() throws IOException {
        // given
        String folder = temp.newFolder().getCanonicalPath();

        // when
        int exitCode = cli.execute("--output", folder);

        // then
        assertThat(exitCode).isEqualTo(2);
        assertThat(stderr).hasToString(INVALID_OUTPUT_FILE_ERROR_MESSAGE);
        assertHelpMessage();
    }

    @Test
    public void noInputFileSpecified() throws IOException {
        // given
        String output = temp.newFile().getCanonicalPath();

        // when
        int exitCode = cli.execute("-o", output);

        // then
        assertThat(exitCode).isEqualTo(2);
        assertThat(stderr).hasToString(NO_INPUT_FILE_ERROR_MESSAGE);
        assertHelpMessage();
    }

    @Test
    public void notEnoughInputFilesSpecified() throws IOException {
        // given
        String output = temp.newFile().getCanonicalPath();
        String input = temp.newFile().getCanonicalPath();
        String expectedErrorMessage = String.format(NOT_ENOUGH_INPUT_FILES_ERROR_MESSAGE_FORMAT, input);

        // when
        int exitCode = cli.execute("-o", output, input);

        // then
        assertThat(exitCode).isEqualTo(2);
        assertThat(stderr).hasToString(expectedErrorMessage);
        assertHelpMessage();
    }

    @Test
    public void invalidInputFiles() throws IOException {
        // given
        String output = temp.newFile().getCanonicalPath();
        String input1 = temp.newFolder().getCanonicalPath();
        String input2 = temp.newFolder().getCanonicalPath();

        // when
        int exitCode = cli.execute("-o", output, input1, input2);

        // then
        assertThat(exitCode).isEqualTo(2);
        assertThat(stderr).hasToString(INVALID_INPUT_FILE_ERROR_MESSAGE);
        assertHelpMessage();
    }

    @Test
    public void notExistingInputFiles() throws IOException {
        // given
        String output = temp.newFile().getCanonicalPath();
        String notExistingInput = temp.newFile().getCanonicalPath() + "test";
        String expectedErrorMessage = String.format(NOT_EXISTING_FILE_ERROR_MESSAGE_FORMAT, notExistingInput);

        // when
        int exitCode = cli.execute("-o", output, notExistingInput, notExistingInput);

        // then
        assertThat(exitCode).isEqualTo(2);
        assertThat(stderr).hasToString(expectedErrorMessage);
        assertHelpMessage();
    }

    @Test
    public void missingForceOption() throws IOException {
        // given
        String output = temp.newFile().getCanonicalPath();
        String input1 = temp.newFile().getCanonicalPath();
        String input2 = temp.newFile().getCanonicalPath();

        // when
        int exitCode = cli.execute("-o", output, input1, input2);

        // then
        assertThat(exitCode).isOne();
        assertThat(stderr).hasToString("pdfmerge: The output file already exists. Use -f or --force to overwrite it.\n");
        assertThat(stdout.toString()).isEmpty();
    }

    @Test
    public void helpShortOptionWorks() {
        // when
        int exitCode = cli.execute("-h");

        // then
        assertThat(exitCode).isZero();
        assertHelpMessage();
    }

    @Test
    public void helpLongOptionWorks() {
        // when
        int exitCode = cli.execute("--help");

        // then
        assertThat(exitCode).isZero();
        assertHelpMessage();
    }

    private void assertHelpMessage() {
        assertThat(stdout.toString()).contains("Usage: pdfmerge [-fhV] -o=OUTFILE FILE FILE...",
                "-f, --force", "-h, --help", "-o, --output=OUTFILE", "-V, --version");
    }

}
