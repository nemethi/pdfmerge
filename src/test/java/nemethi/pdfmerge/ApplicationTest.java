package nemethi.pdfmerge;

import nemethi.pdfmerge.util.FileChecker;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParameterException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.util.Lists.list;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationTest {

    private static final IOException EXPECTED_EXCEPTION = new IOException("message");

    @Mock
    private PdfMerger pdfMerger;
    @Mock
    private FileChecker fileChecker;
    @Mock
    private CommandSpec spec;
    @Mock(stubOnly = true)
    private CommandLine commandLine;
    @Mock(stubOnly = true)
    private Path invalidPath;
    @Mock(stubOnly = true)
    private Path validPath;
    @Mock(stubOnly = true)
    private Path validPath2;

    private Application application;

    @Before
    public void setUp() throws Exception {
        application = new Application(pdfMerger);
        application.setFileChecker(fileChecker);
        application.setSpec(spec);
    }

    @Test
    public void setOutputFileThrowsExceptionIfPathIsDirectory() {
        // given
        when(fileChecker.isDirectory(invalidPath)).thenReturn(true);
        when(spec.commandLine()).thenReturn(commandLine);

        // when
        Throwable thrown = catchThrowable(() -> application.setOutputFile(invalidPath));

        // then
        assertThat(thrown)
                .isInstanceOf(ParameterException.class)
                .hasMessage("Invalid path: OUTFILE must point to a file.");
        verify(fileChecker).isDirectory(invalidPath);
        verify(spec).commandLine();
        verifyNoMoreInteractions(fileChecker, spec);
    }

    @Test
    public void setOutputFileSetsOutputFile() {
        // given
        when(fileChecker.isDirectory(validPath)).thenReturn(false);

        // when
        application.setOutputFile(validPath);

        // then
        assertThat(application.getOutputFile()).isEqualTo(validPath);
        verify(fileChecker).isDirectory(validPath);
        verifyNoMoreInteractions(fileChecker);
        verifyNoInteractions(spec);
    }

    @Test
    public void setInputFilesThrowsExceptionIfAnyPathDoesNotExist() {
        // given
        List<Path> paths = list(validPath, invalidPath);
        when(fileChecker.notExists(validPath)).thenReturn(false);
        when(fileChecker.isDirectory(validPath)).thenReturn(false);
        when(fileChecker.notExists(invalidPath)).thenReturn(true);
        when(spec.commandLine()).thenReturn(commandLine);

        // when
        Throwable thrown = catchThrowable(() -> application.setInputFiles(paths));

        // then
        assertThat(thrown)
                .isInstanceOf(ParameterException.class)
                .hasMessageContainingAll("Invalid path:", "does not exist.");
        verify(fileChecker).notExists(validPath);
        verify(fileChecker).isDirectory(validPath);
        verify(fileChecker).notExists(invalidPath);
        verify(spec).commandLine();
        verifyNoMoreInteractions(fileChecker, spec);
    }

    @Test
    public void setInputFilesThrowsExceptionIfAnyPathIsADirectory() {
        // given
        List<Path> paths = list(validPath, invalidPath);
        when(fileChecker.notExists(validPath)).thenReturn(false);
        when(fileChecker.isDirectory(validPath)).thenReturn(false);
        when(fileChecker.notExists(invalidPath)).thenReturn(false);
        when(fileChecker.isDirectory(invalidPath)).thenReturn(true);
        when(spec.commandLine()).thenReturn(commandLine);

        // when
        Throwable thrown = catchThrowable(() -> application.setInputFiles(paths));

        // then
        assertThat(thrown)
                .isInstanceOf(ParameterException.class)
                .hasMessage("Invalid path: FILE must point to a file.");
        verify(fileChecker).notExists(validPath);
        verify(fileChecker).isDirectory(validPath);
        verify(fileChecker).notExists(invalidPath);
        verify(fileChecker).isDirectory(invalidPath);
        verify(spec).commandLine();
        verifyNoMoreInteractions(fileChecker, spec);
    }

    @Test
    public void setInputFilesSetsInputFiles() {
        // given
        List<Path> paths = list(validPath, validPath2);
        when(fileChecker.notExists(validPath)).thenReturn(false);
        when(fileChecker.isDirectory(validPath)).thenReturn(false);
        when(fileChecker.notExists(validPath2)).thenReturn(false);
        when(fileChecker.isDirectory(validPath2)).thenReturn(false);

        // when
        application.setInputFiles(paths);

        // then
        assertThat(application.getInputFiles()).isEqualTo(paths);
        verify(fileChecker).notExists(validPath);
        verify(fileChecker).isDirectory(validPath);
        verify(fileChecker).notExists(validPath2);
        verify(fileChecker).isDirectory(validPath2);
        verifyNoMoreInteractions(fileChecker);
        verifyNoInteractions(spec);
    }

    @Test
    public void callInvokesForceMerge() throws Exception {
        // given
        List<Path> paths = list(validPath, validPath2);
        mockFileChecks();
        application.setOutputFile(validPath);
        application.setInputFiles(paths);
        application.setForced(true);

        // when
        int exitCode = application.call();

        // then
        assertThat(exitCode).isZero();
        verify(pdfMerger).forceMerge(paths, validPath);
        verifyNoMoreInteractions(pdfMerger);
    }

    @Test
    public void callForwardsForceMergeException() throws IOException {
        // given
        doThrow(EXPECTED_EXCEPTION).when(pdfMerger).forceMerge(any(), any());
        application.setForced(true);

        // when
        Throwable thrown = catchThrowable(() -> application.call());

        // then
        assertThat(thrown).isEqualTo(EXPECTED_EXCEPTION);
        verify(pdfMerger).forceMerge(any(), any());
        verifyNoMoreInteractions(pdfMerger);
    }

    @Test
    public void callInvokesMerge() throws Exception {
        // given
        List<Path> paths = list(validPath, validPath2);
        mockFileChecks();
        application.setOutputFile(validPath);
        application.setInputFiles(paths);
        application.setForced(false);

        // when
        int exitCode = application.call();

        // then
        assertThat(exitCode).isZero();
        verify(pdfMerger).merge(paths, validPath);
        verifyNoMoreInteractions(pdfMerger);
    }

    @Test
    public void callForwardsMergeException() throws IOException {
        // given
        doThrow(EXPECTED_EXCEPTION).when(pdfMerger).merge(any(), any());
        application.setForced(false);

        // when
        Throwable thrown = catchThrowable(() -> application.call());

        // then
        assertThat(thrown).isEqualTo(EXPECTED_EXCEPTION);
        verify(pdfMerger).merge(any(), any());
        verifyNoMoreInteractions(pdfMerger);
    }

    private void mockFileChecks() {
        when(fileChecker.isDirectory(validPath)).thenReturn(false);
        when(fileChecker.notExists(validPath)).thenReturn(false);
        when(fileChecker.isDirectory(validPath)).thenReturn(false);
        when(fileChecker.notExists(validPath2)).thenReturn(false);
        when(fileChecker.isDirectory(validPath2)).thenReturn(false);
    }
}
