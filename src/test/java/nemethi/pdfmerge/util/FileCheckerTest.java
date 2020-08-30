package nemethi.pdfmerge.util;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class FileCheckerTest {

    private static final Path NOT_EXISTING_PATH = Paths.get("invalid-path");

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private FileChecker fileChecker;

    @Before
    public void setUp() {
        fileChecker = new FileChecker();
    }

    @Test
    public void existsReturnsTrueWhenPathExists() throws IOException {
        // given
        Path existingPath = temp.newFile().toPath();

        // when + then
        assertThat(fileChecker.exists(existingPath)).isTrue();
    }

    @Test
    public void existsReturnsFalseWhenPathDoesNotExist() {
        assertThat(fileChecker.exists(NOT_EXISTING_PATH)).isFalse();
    }

    @Test
    public void notExistsReturnsTrueWhenPathDoesNotExist() {
        assertThat(fileChecker.notExists(NOT_EXISTING_PATH)).isTrue();
    }

    @Test
    public void notExistsReturnsFalseWhenPathExists() throws IOException {
        // given
        Path existingPath = temp.newFile().toPath();

        // when + then
        assertThat(fileChecker.notExists(existingPath)).isFalse();
    }

    @Test
    public void isDirectoryReturnsFalseWhenPathDoesNotExist() {
        assertThat(fileChecker.isDirectory(NOT_EXISTING_PATH)).isFalse();
    }

    @Test
    public void isDirectoryReturnsFalseWhenPathIsNotADirectory() throws IOException {
        // given
        Path existingPath = temp.newFile().toPath();

        // when + then
        assertThat(fileChecker.isDirectory(existingPath)).isFalse();
    }

    @Test
    public void isDirectoryReturnsTrueWhenPathIsADirectory() throws IOException {
        // given
        Path existingPath = temp.newFolder().toPath();

        // when + then
        assertThat(fileChecker.isDirectory(existingPath)).isTrue();
    }
}
