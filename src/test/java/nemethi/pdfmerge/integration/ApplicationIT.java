package nemethi.pdfmerge.integration;

import nemethi.pdfmerge.Application;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.Assertion;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.contrib.java.lang.system.SystemErrRule;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationIT {

    private static final String OUTPUT_FILE_EXISTS_ERROR_MESSAGE = "pdfmerge: The output file already exists. Use -f or --force to overwrite it.\n";
    private static final String TEST_FILE_NAME_PREFIX = "pdfmerge-test-";
    private static final String TEST_FILE_NAME_SUFFIX = ".pdf";
    private static final String FIRST_PAGE_CONTENT = "First page";
    private static final String SECOND_PAGE_CONTENT = "Second page";
    private static final String THIRD_PAGE_CONTENT = "Third page";

    @Rule
    public ExpectedSystemExit exit = ExpectedSystemExit.none();
    @Rule
    public SystemErrRule stderr = new SystemErrRule().enableLog();

    private static Path testTempDir;
    private static Path pdf1;
    private static Path pdf2;

    @BeforeClass
    public static void beforeClass() throws Exception {
        testTempDir = Files.createTempDirectory(Paths.get(System.getProperty("java.io.tmpdir")), TEST_FILE_NAME_PREFIX);
        pdf1 = Files.createTempFile(testTempDir, TEST_FILE_NAME_PREFIX, TEST_FILE_NAME_SUFFIX);
        pdf2 = Files.createTempFile(testTempDir, TEST_FILE_NAME_PREFIX, TEST_FILE_NAME_SUFFIX);
        createPdf(pdf1.toFile(), FIRST_PAGE_CONTENT);
        createPdf(pdf2.toFile(), SECOND_PAGE_CONTENT, THIRD_PAGE_CONTENT);
    }

    @AfterClass
    public static void afterClass() throws IOException {
        File[] files = testTempDir.toFile().listFiles();
        if (files != null) {
            for (File file : files) {
                Files.delete(file.toPath());
            }
        }
        Files.delete(testTempDir);
    }

    @Test
    public void mergeTwoPdfs() {
        // given
        Path outputFile = Paths.get(testTempDir.toString(), randomFilename());

        // when + then
        exit.expectSystemExitWithStatus(0);
        exit.checkAssertionAfterwards(assertThatPdfsAreMerged(outputFile, pdf1, pdf2));
        Application.main(args("-o", outputFile.toString(), pdf1.toString(), pdf2.toString()));
    }

    @Test
    public void mergeSameInputPdfMultipleTimes() {
        // given
        Path outputFile = Paths.get(testTempDir.toString(), randomFilename());

        // when + then
        exit.expectSystemExitWithStatus(0);
        exit.checkAssertionAfterwards(assertThatPdfsAreMerged(outputFile, pdf1, pdf1));
        Application.main(args("-o", outputFile.toString(), pdf1.toString(), pdf1.toString()));
    }

    @Test
    public void mergeFailsIfOutputFileExists() throws IOException {
        // given
        Path outputFile = Files.createTempFile(testTempDir, TEST_FILE_NAME_PREFIX, TEST_FILE_NAME_SUFFIX);

        // when + then
        exit.expectSystemExitWithStatus(1);
        exit.checkAssertionAfterwards(() -> assertThat(stderr.getLog()).isEqualTo(OUTPUT_FILE_EXISTS_ERROR_MESSAGE));
        Application.main(args("-o", outputFile.toString(), pdf1.toString(), pdf2.toString()));
    }

    @Test
    public void mergeWithForceShortOption() throws IOException {
        // given
        Path outputFile = Files.createTempFile(testTempDir, TEST_FILE_NAME_PREFIX, TEST_FILE_NAME_SUFFIX);

        // when + then
        exit.expectSystemExitWithStatus(0);
        exit.checkAssertionAfterwards(assertThatPdfsAreMerged(outputFile, pdf1, pdf2));
        Application.main(args("-f", "-o", outputFile.toString(), pdf1.toString(), pdf2.toString()));
    }

    @Test
    public void mergeWithForceLongOption() throws IOException {
        // given
        Path outputFile = Files.createTempFile(testTempDir, TEST_FILE_NAME_PREFIX, TEST_FILE_NAME_SUFFIX);

        // when + then
        exit.expectSystemExitWithStatus(0);
        exit.checkAssertionAfterwards(assertThatPdfsAreMerged(outputFile, pdf1, pdf2));
        Application.main(args("--force", "-o", outputFile.toString(), pdf1.toString(), pdf2.toString()));
    }

    private String[] args(String... args) {
        return args;
    }

    private Assertion assertThatPdfsAreMerged(Path outputFile, Path... inputFiles) {
        return () -> assertThat(contentOf(outputFile)).isEqualTo(contentOf(inputFiles));
    }

    private String contentOf(Path... inputFiles) throws IOException {
        StringBuilder builder = new StringBuilder();
        for (Path inputFile : inputFiles) {
            builder.append(getTextFromPdf(inputFile));
        }
        return builder.toString();
    }

    private String getTextFromPdf(Path pdf) throws IOException {
        try (PDDocument document = PDDocument.load(pdf.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private static void createPdf(File target, String... pages) throws IOException {
        try (PDDocument document = new PDDocument()) {
            for (String content : pages) {
                document.addPage(createPage(document, content));
            }
            document.save(target);
        }
    }

    private static PDPage createPage(PDDocument document, String content) throws IOException {
        final int fontSize = 72;
        final int textXOffset = 100;
        final int textYOffset = 700;
        PDPage page = new PDPage();
        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, fontSize);
            contentStream.newLineAtOffset(textXOffset, textYOffset);
            contentStream.showText(content);
            contentStream.endText();
        }
        return page;
    }

    private String randomFilename() {
        final int numOfInts = 16;
        final int lowerBound = 0;
        final int upperBound = 10;
        String fileName = new Random().ints(numOfInts, lowerBound, upperBound).mapToObj(String::valueOf)
                .collect(Collectors.joining());
        return TEST_FILE_NAME_PREFIX + fileName + TEST_FILE_NAME_SUFFIX;
    }
}
