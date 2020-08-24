package nemethi.pdfmerge.util;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.emptyList;
import static org.assertj.core.util.Lists.list;

public class PathToStreamConverterTest {

    private static final String EMPTY_PATH_NAME = "";

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private PathToStreamConverter converter;

    @Before
    public void setUp() {
        converter = new PathToStreamConverter();
    }

    @Test
    public void returnEmptyListOnEmptyInputList() throws FileNotFoundException {
        assertThat(converter.convertPathsToStreams(emptyList())).isEmpty();
    }

    @Test
    public void returnStreamListForPaths() throws IOException {
        // given
        List<Path> paths = list(temp.newFile().toPath(), temp.newFile().toPath());

        // when
        List<InputStream> streams = converter.convertPathsToStreams(paths);

        // then
        assertThat(streams).hasSameSizeAs(paths).hasOnlyElementsOfType(FileInputStream.class);
    }

    @Test
    public void throwsExceptionOnInvalidPath() throws IOException {
        // given
        List<Path> paths = list(temp.newFile().toPath(), Paths.get(EMPTY_PATH_NAME));

        // when + then
        thrown.expect(FileNotFoundException.class);
        converter.convertPathsToStreams(paths);
    }
}
