package nemethi.pdfmerge.util;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class OutputStreamSupplierTest {

    private static final String EMPTY_PATH_NAME = "";

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private OutputStreamSupplier supplier;

    @Before
    public void setUp() {
        supplier = new OutputStreamSupplier();
    }

    @Test
    public void returnsOutputStreamToExistingFile() throws IOException {
        // given
        Path existingFile = temp.newFile().toPath();

        // when + then
        assertThat(supplier.getFileStream(existingFile)).isInstanceOf(FileOutputStream.class);
    }

    @Test
    public void throwsExceptionOnInvalidFile() throws FileNotFoundException {
        // given
        Path invalidPath = Paths.get(EMPTY_PATH_NAME);

        // when + then
        thrown.expect(FileNotFoundException.class);
        supplier.getFileStream(invalidPath);
    }
}
