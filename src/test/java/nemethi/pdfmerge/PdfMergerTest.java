package nemethi.pdfmerge;

import nemethi.pdfmerge.util.FileChecker;
import nemethi.pdfmerge.util.OutputStreamSupplier;
import nemethi.pdfmerge.util.PathToStreamConverter;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.util.Lists.list;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
public class PdfMergerTest {

    @Mock
    private PDFMergerUtility mergerUtility;
    @Mock
    private Path inputPath1;
    @Mock
    private Path inputPath2;
    @Mock
    private Path outputPath;
    @Mock
    private PathToStreamConverter converter;
    @Mock
    private FileChecker fileChecker;
    @Mock
    private OutputStreamSupplier streamSupplier;
    @Mock
    private OutputStream outputStream;
    @Mock
    private InputStream inputStream1;
    @Mock
    private InputStream inputStream2;

    private PdfMerger pdfMerger;

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private List<Path> inputPaths;
    private List<InputStream> inputStreams;

    @Before
    public void setUp() {
        initMocks(this);
        pdfMerger = new PdfMerger(mergerUtility);
        pdfMerger.setConverter(converter);
        pdfMerger.setFileChecker(fileChecker);
        pdfMerger.setStreamSupplier(streamSupplier);
        inputPaths = list(inputPath1, inputPath2);
        inputStreams = list(inputStream1, inputStream2);
    }

    @Test
    public void merge() throws IOException {
        // given
        when(fileChecker.exists(outputPath)).thenReturn(false);
        when(converter.convertPathsToStreams(inputPaths)).thenReturn(inputStreams);
        when(streamSupplier.getFileStream(outputPath)).thenReturn(outputStream);

        // when
        pdfMerger.merge(inputPaths, outputPath);

        // then
        verify(converter).convertPathsToStreams(inputPaths);
        verify(fileChecker).exists(outputPath);
        verify(streamSupplier).getFileStream(outputPath);
        verify(mergerUtility).addSources(inputStreams);
        verify(mergerUtility).setDestinationStream(outputStream);
        verify(mergerUtility).mergeDocuments(any(MemoryUsageSetting.class));
        verifyNoMoreInteractions(mergerUtility);
    }

    @Test
    public void mergeThrowsExceptionOnExistingOutputFile() throws IOException {
        // given
        when(fileChecker.exists(outputPath)).thenReturn(true);

        // when + then
        thrown.expect(FileAlreadyExistsException.class);
        thrown.expectMessage(is("The output file already exists"));
        pdfMerger.merge(inputPaths, outputPath);

        // then
        verify(fileChecker).exists(outputPath);
    }

    @Test
    public void forceMergeOverwritesOutputFile() throws IOException {
        // given
        when(converter.convertPathsToStreams(inputPaths)).thenReturn(inputStreams);
        when(streamSupplier.getFileStream(outputPath)).thenReturn(outputStream);

        // when
        pdfMerger.forceMerge(inputPaths, outputPath);

        // then
        verify(converter).convertPathsToStreams(inputPaths);
        verify(streamSupplier).getFileStream(outputPath);
        verify(mergerUtility).addSources(inputStreams);
        verify(mergerUtility).setDestinationStream(outputStream);
        verify(mergerUtility).mergeDocuments(any(MemoryUsageSetting.class));
        verifyNoMoreInteractions(mergerUtility);
        verifyNoInteractions(fileChecker);
    }
}
