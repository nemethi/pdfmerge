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

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private FileChecker fileChecker;

    @Before
    public void setUp() {
        fileChecker = new FileChecker();
    }

    @Test
    public void returnsTrueWhenFileExists() throws IOException {
        // given
        Path validFile = temp.newFile().toPath();

        // when + then
        assertThat(fileChecker.exists(validFile)).isTrue();
    }

    @Test
    public void returnsFalseWhenFileDoesNotExist() {
        // given
        Path invalidFile = Paths.get("invalid-file");

        // when + then
        assertThat(fileChecker.exists(invalidFile)).isFalse();
    }
}
