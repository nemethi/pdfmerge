package nemethi.pdfmerge.cli;

import picocli.CommandLine;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.IParameterExceptionHandler;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.UnmatchedArgumentException;

import java.io.PrintWriter;
import java.nio.file.FileAlreadyExistsException;

public class ExceptionHandler implements IExecutionExceptionHandler, IParameterExceptionHandler {

    private static final String ERROR_MESSAGE_FORMAT = "%s: %s";
    private static final String ERROR_MESSAGE_WITH_HINT_FORMAT = "%s: %s %s";
    private static final String FORCE_HINT = "Use -f or --force to overwrite it.";

    @Override
    public int handleParseException(ParameterException ex, String[] args) throws Exception {
        CommandLine commandLine = ex.getCommandLine();
        String errorMessage = String.format(ERROR_MESSAGE_FORMAT, commandLine.getCommandName(), ex.getMessage());
        commandLine.getErr().println(commandLine.getColorScheme().errorText(errorMessage));
        if (!suggestionsArePrinted(ex, commandLine.getErr())) {
            commandLine.usage(commandLine.getOut(), commandLine.getColorScheme());
        }

        if (commandLine.getExitCodeExceptionMapper() != null) {
            return commandLine.getExitCodeExceptionMapper().getExitCode(ex);
        } else {
            return commandLine.getCommandSpec().exitCodeOnInvalidInput();
        }
    }

    @Override
    public int handleExecutionException(Exception ex, CommandLine commandLine, ParseResult parseResult) throws Exception {
        if (ex instanceof FileAlreadyExistsException) {
            handleFileAlreadyExistsException(ex, commandLine);
        } else {
            String errorMessage = String.format(ERROR_MESSAGE_FORMAT, commandLine.getCommandName(), ex.getMessage());
            commandLine.getErr().println(commandLine.getColorScheme().errorText(errorMessage));
        }

        if (commandLine.getExitCodeExceptionMapper() != null) {
            return commandLine.getExitCodeExceptionMapper().getExitCode(ex);
        } else {
            return commandLine.getCommandSpec().exitCodeOnExecutionException();
        }
    }

    boolean suggestionsArePrinted(ParameterException ex, PrintWriter err) {
        return UnmatchedArgumentException.printSuggestions(ex, err);
    }

    private void handleFileAlreadyExistsException(Exception ex, CommandLine commandLine) {
        String errorMessage = String.format(ERROR_MESSAGE_WITH_HINT_FORMAT,
                commandLine.getCommandName(), ex.getMessage(), FORCE_HINT);
        commandLine.getErr().println(commandLine.getColorScheme().errorText(errorMessage));
    }
}
