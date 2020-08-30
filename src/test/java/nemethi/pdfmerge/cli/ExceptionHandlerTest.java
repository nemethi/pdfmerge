package nemethi.pdfmerge.cli;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import picocli.CommandLine;
import picocli.CommandLine.IExitCodeExceptionMapper;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ParseResult;

import java.io.PrintWriter;
import java.nio.file.FileAlreadyExistsException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExceptionHandlerTest {

    private static final String ERROR_MESSAGE_FORMAT = "%s: %s";
    private static final String ERROR_MESSAGE_WITH_HINT_FORMAT = "%s: %s %s";
    private static final String COMMAND_NAME = "testCommand";
    private static final String ERROR_MESSAGE = "error message";
    private static final String FORCE_HINT = "Use -f or --force to overwrite it.";
    private static final String INVALID_PARAMETER_MESSAGE = String.format(ERROR_MESSAGE_FORMAT, COMMAND_NAME, ERROR_MESSAGE);
    private static final String EXECUTION_ERROR_MESSAGE = String.format(ERROR_MESSAGE_FORMAT, COMMAND_NAME, ERROR_MESSAGE);
    private static final String EXECUTION_ERROR_MESSAGE_WITH_HINT = String.format(ERROR_MESSAGE_WITH_HINT_FORMAT, COMMAND_NAME, ERROR_MESSAGE, FORCE_HINT);
    private static final int INVALID_PARAMETER_EXIT_CODE = 2;
    private static final int EXECUTION_ERROR_EXIT_CODE = 1;
    private static final String[] STUB_ARRAY = new String[0];

    @Mock
    private ParameterException parameterException;
    @Mock
    private FileAlreadyExistsException fileAlreadyExistsException;
    @Mock
    private Exception exception;
    @Mock
    private CommandLine commandLine;
    @Mock
    private CommandSpec commandSpec;
    @Mock
    private CommandLine.Help.ColorScheme colorScheme;
    @Mock
    private PrintWriter errorWriter;
    @Mock
    private IExitCodeExceptionMapper exitCodeExceptionMapper;
    @Mock(stubOnly = true)
    private ParseResult parseResult;
    @Mock(stubOnly = true)
    private CommandLine.Help.Ansi.Text text;
    @Mock(stubOnly = true)
    private PrintWriter outWriter;

    @Spy
    private ExceptionHandler exceptionHandler;

    @Test
    public void handleParseExceptionWithoutSuggestionsAndWithExceptionMapper() throws Exception {
        // given
        mockForHandleParseException();
        mockSuggestionsArePrinted(false);
        when(commandLine.getOut()).thenReturn(outWriter);
        when(commandLine.getExitCodeExceptionMapper()).thenReturn(exitCodeExceptionMapper);
        when(exitCodeExceptionMapper.getExitCode(parameterException)).thenReturn(INVALID_PARAMETER_EXIT_CODE);

        // when
        int exitCode = exceptionHandler.handleParseException(parameterException, STUB_ARRAY);

        // then
        assertThat(exitCode).isEqualTo(INVALID_PARAMETER_EXIT_CODE);
        verifyForHandleParseException();
        verify(commandLine, times(2)).getColorScheme();
        verify(colorScheme).errorText(INVALID_PARAMETER_MESSAGE);
        verify(errorWriter).println(text);
        verify(commandLine).getOut();
        verify(commandLine).usage(outWriter, colorScheme);
        verify(commandLine, times(2)).getExitCodeExceptionMapper();
        verify(exitCodeExceptionMapper).getExitCode(parameterException);
        verifyNoMoreInteractions(parameterException, commandLine, colorScheme, errorWriter, exitCodeExceptionMapper);
    }

    @Test
    public void handleParseExceptionWithSuggestionsAndWithExceptionMapper() throws Exception {
        // given
        mockForHandleParseException();
        mockSuggestionsArePrinted(true);
        when(commandLine.getExitCodeExceptionMapper()).thenReturn(exitCodeExceptionMapper);
        when(exitCodeExceptionMapper.getExitCode(parameterException)).thenReturn(INVALID_PARAMETER_EXIT_CODE);

        // when
        int exitCode = exceptionHandler.handleParseException(parameterException, STUB_ARRAY);

        // then
        assertThat(exitCode).isEqualTo(INVALID_PARAMETER_EXIT_CODE);
        verifyForHandleParseException();
        verify(commandLine).getColorScheme();
        verify(colorScheme).errorText(INVALID_PARAMETER_MESSAGE);
        verify(errorWriter).println(text);
        verify(commandLine, times(2)).getExitCodeExceptionMapper();
        verify(exitCodeExceptionMapper).getExitCode(parameterException);
        verifyNoMoreInteractions(parameterException, commandLine, colorScheme, errorWriter);
    }

    @Test
    public void handleParseExceptionWithSuggestionsAndWithoutExceptionMapper() throws Exception {
        // given
        mockForHandleParseException();
        mockSuggestionsArePrinted(true);
        when(commandLine.getExitCodeExceptionMapper()).thenReturn(null);
        when(commandLine.getCommandSpec()).thenReturn(commandSpec);
        when(commandSpec.exitCodeOnInvalidInput()).thenReturn(INVALID_PARAMETER_EXIT_CODE);

        // when
        int exitCode = exceptionHandler.handleParseException(parameterException, STUB_ARRAY);

        // then
        assertThat(exitCode).isEqualTo(INVALID_PARAMETER_EXIT_CODE);
        verifyForHandleParseException();
        verify(commandLine).getColorScheme();
        verify(colorScheme).errorText(INVALID_PARAMETER_MESSAGE);
        verify(errorWriter).println(text);
        verify(commandLine).getExitCodeExceptionMapper();
        verify(commandLine).getCommandSpec();
        verify(commandSpec).exitCodeOnInvalidInput();
        verifyNoMoreInteractions(parameterException, commandLine, colorScheme, errorWriter, commandSpec);
    }

    @Test
    public void handleParseExceptionWithoutSuggestionsAndWithoutExceptionMapper() throws Exception {
        // given
        mockForHandleParseException();
        mockSuggestionsArePrinted(false);
        when(commandLine.getOut()).thenReturn(outWriter);
        when(commandLine.getExitCodeExceptionMapper()).thenReturn(null);
        when(commandLine.getCommandSpec()).thenReturn(commandSpec);
        when(commandSpec.exitCodeOnInvalidInput()).thenReturn(INVALID_PARAMETER_EXIT_CODE);

        // when
        int exitCode = exceptionHandler.handleParseException(parameterException, STUB_ARRAY);

        // then
        assertThat(exitCode).isEqualTo(INVALID_PARAMETER_EXIT_CODE);
        verifyForHandleParseException();
        verify(commandLine, times(2)).getColorScheme();
        verify(colorScheme).errorText(INVALID_PARAMETER_MESSAGE);
        verify(errorWriter).println(text);
        verify(commandLine).getOut();
        verify(commandLine).usage(outWriter, colorScheme);
        verify(commandLine).getExitCodeExceptionMapper();
        verify(commandLine).getCommandSpec();
        verify(commandSpec).exitCodeOnInvalidInput();
        verifyNoMoreInteractions(parameterException, commandLine, colorScheme, errorWriter, commandSpec);
    }

    @Test
    public void handleFileAlreadyExistsExceptionWithExceptionMapper() throws Exception {
        // given
        mockForHandleFileAlreadyExistsException();
        when(commandLine.getExitCodeExceptionMapper()).thenReturn(exitCodeExceptionMapper);
        when(exitCodeExceptionMapper.getExitCode(fileAlreadyExistsException)).thenReturn(EXECUTION_ERROR_EXIT_CODE);

        // when
        int exitCode = exceptionHandler.handleExecutionException(fileAlreadyExistsException, commandLine, parseResult);

        // then
        assertThat(exitCode).isEqualTo(EXECUTION_ERROR_EXIT_CODE);
        verifyHandleFileAlreadyExistsException();
        verify(commandLine, times(2)).getExitCodeExceptionMapper();
        verify(exitCodeExceptionMapper).getExitCode(fileAlreadyExistsException);
        verifyNoMoreInteractions(fileAlreadyExistsException, commandLine, colorScheme, exitCodeExceptionMapper);
    }

    @Test
    public void handleFileAlreadyExistsExceptionWithoutExceptionMapper() throws Exception {
        // given
        mockForHandleFileAlreadyExistsException();
        when(commandLine.getExitCodeExceptionMapper()).thenReturn(null);
        when(commandLine.getCommandSpec()).thenReturn(commandSpec);
        when(commandSpec.exitCodeOnExecutionException()).thenReturn(EXECUTION_ERROR_EXIT_CODE);

        // when
        int exitCode = exceptionHandler.handleExecutionException(fileAlreadyExistsException, commandLine, parseResult);

        // then
        assertThat(exitCode).isEqualTo(EXECUTION_ERROR_EXIT_CODE);
        verifyHandleFileAlreadyExistsException();
        verify(commandLine).getExitCodeExceptionMapper();
        verify(commandLine).getCommandSpec();
        verify(commandSpec).exitCodeOnExecutionException();
        verifyNoMoreInteractions(commandLine, fileAlreadyExistsException, colorScheme, errorWriter, commandSpec);
    }

    @Test
    public void handleExecutionExceptionWithoutExceptionMapper() throws Exception {
        // given
        mockForHandleExecutionException();
        when(commandLine.getExitCodeExceptionMapper()).thenReturn(null);
        when(commandLine.getCommandSpec()).thenReturn(commandSpec);
        when(commandSpec.exitCodeOnExecutionException()).thenReturn(EXECUTION_ERROR_EXIT_CODE);

        // when
        int exitCode = exceptionHandler.handleExecutionException(exception, commandLine, parseResult);

        // then
        assertThat(exitCode).isEqualTo(EXECUTION_ERROR_EXIT_CODE);
        verifyForHandleExecutionException();
        verify(commandLine).getExitCodeExceptionMapper();
        verify(commandLine).getCommandSpec();
        verify(commandSpec).exitCodeOnExecutionException();
        verifyNoMoreInteractions(commandLine, exception, colorScheme, errorWriter, commandSpec);
    }

    @Test
    public void handleExecutionExceptionWithExceptionMapper() throws Exception {
        // given
        mockForHandleExecutionException();
        when(commandLine.getExitCodeExceptionMapper()).thenReturn(exitCodeExceptionMapper);
        when(exitCodeExceptionMapper.getExitCode(exception)).thenReturn(EXECUTION_ERROR_EXIT_CODE);

        // when
        int exitCode = exceptionHandler.handleExecutionException(exception, commandLine, parseResult);

        // then
        assertThat(exitCode).isEqualTo(EXECUTION_ERROR_EXIT_CODE);
        verifyForHandleExecutionException();
        verify(commandLine, times(2)).getExitCodeExceptionMapper();
        verify(exitCodeExceptionMapper).getExitCode(exception);
        verifyNoMoreInteractions(commandLine, exception, colorScheme, errorWriter, exitCodeExceptionMapper);
    }

    private void mockForHandleParseException() {
        when(parameterException.getCommandLine()).thenReturn(commandLine);
        when(commandLine.getCommandName()).thenReturn(COMMAND_NAME);
        when(parameterException.getMessage()).thenReturn(ERROR_MESSAGE);
        when(commandLine.getErr()).thenReturn(errorWriter);
        when(commandLine.getColorScheme()).thenReturn(colorScheme);
        when(colorScheme.errorText(INVALID_PARAMETER_MESSAGE)).thenReturn(text);
    }

    private void mockSuggestionsArePrinted(boolean value) {
        doReturn(value).when(exceptionHandler).suggestionsArePrinted(parameterException, errorWriter);
    }

    private void verifyForHandleParseException() {
        verify(parameterException).getCommandLine();
        verify(commandLine).getCommandName();
        verify(parameterException).getMessage();
        verify(commandLine, times(2)).getErr();
    }

    private void mockForHandleFileAlreadyExistsException() {
        when(commandLine.getCommandName()).thenReturn(COMMAND_NAME);
        when(fileAlreadyExistsException.getMessage()).thenReturn(ERROR_MESSAGE);
        when(commandLine.getErr()).thenReturn(errorWriter);
        when(commandLine.getColorScheme()).thenReturn(colorScheme);
        when(colorScheme.errorText(EXECUTION_ERROR_MESSAGE_WITH_HINT)).thenReturn(text);
    }

    private void verifyHandleFileAlreadyExistsException() {
        verify(commandLine).getCommandName();
        verify(fileAlreadyExistsException).getMessage();
        verify(commandLine).getErr();
        verify(commandLine).getColorScheme();
        verify(colorScheme).errorText(EXECUTION_ERROR_MESSAGE_WITH_HINT);
        verify(errorWriter).println(text);
    }

    private void mockForHandleExecutionException() {
        when(commandLine.getCommandName()).thenReturn(COMMAND_NAME);
        when(exception.getMessage()).thenReturn(ERROR_MESSAGE);
        when(commandLine.getErr()).thenReturn(errorWriter);
        when(commandLine.getColorScheme()).thenReturn(colorScheme);
        when(colorScheme.errorText(EXECUTION_ERROR_MESSAGE)).thenReturn(text);
    }

    private void verifyForHandleExecutionException() {
        verify(commandLine).getCommandName();
        verify(exception).getMessage();
        verify(commandLine).getErr();
        verify(commandLine).getColorScheme();
        verify(colorScheme).errorText(EXECUTION_ERROR_MESSAGE);
        verify(errorWriter).println(text);
    }
}
