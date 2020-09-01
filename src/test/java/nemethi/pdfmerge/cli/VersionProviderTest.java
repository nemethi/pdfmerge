package nemethi.pdfmerge.cli;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import static java.util.Collections.enumeration;
import static java.util.jar.Attributes.Name.IMPLEMENTATION_TITLE;
import static java.util.jar.Attributes.Name.IMPLEMENTATION_VERSION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.util.Lists.list;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class VersionProviderTest {

    private static final String APPLICATION_NAME = "pdfmerge";
    private static final String MANIFEST_FILE = "META-INF/MANIFEST.MF";
    private static final String TEST = "test";
    private static final String VERSION = "version";
    private static final String ERROR_OCCURRED_MESSAGE = "An error occurred during reading the version.";
    private static final String NO_VERSION_ERROR_MESSAGE = "Could not find version information.";

    @Mock
    private URLClassLoader classLoader;
    @Mock
    private URL goodUrl;
    @Mock
    private URL badUrl;
    @Mock
    private Manifest goodManifest;
    @Mock
    private Manifest badManifest;
    @Mock
    private Attributes goodAttributes;
    @Mock
    private Attributes badAttributes;
    private Enumeration<URL> urls;

    @Spy
    private VersionProvider versionProvider;

    @Before
    public void setUp() throws Exception {
        urls = enumeration(list(badUrl, goodUrl));
    }

    @Test
    public void returnsVersionFromManifestFile() throws Exception {
        // given
        doReturn(classLoader).when(versionProvider).getClassLoader();
        when(classLoader.findResources(MANIFEST_FILE)).thenReturn(urls);
        doReturn(badManifest).when(versionProvider).openManifest(badUrl);
        when(badManifest.getMainAttributes()).thenReturn(badAttributes);
        when(badAttributes.getValue(IMPLEMENTATION_TITLE)).thenReturn(TEST);
        doReturn(goodManifest).when(versionProvider).openManifest(goodUrl);
        when(goodManifest.getMainAttributes()).thenReturn(goodAttributes);
        when(goodAttributes.getValue(IMPLEMENTATION_TITLE)).thenReturn(APPLICATION_NAME);
        when(goodAttributes.getValue(IMPLEMENTATION_VERSION)).thenReturn(VERSION);

        // when
        String[] version = versionProvider.getVersion();

        // then
        assertThat(version).isEqualTo(new String[]{VERSION});
        verify(versionProvider).getClassLoader();
        verify(classLoader).findResources(MANIFEST_FILE);
        verify(versionProvider).openManifest(badUrl);
        verify(badManifest).getMainAttributes();
        verify(badAttributes).getValue(IMPLEMENTATION_TITLE);
        verify(versionProvider).openManifest(goodUrl);
        verify(goodManifest).getMainAttributes();
        verify(goodAttributes).getValue(IMPLEMENTATION_TITLE);
        verify(goodAttributes).getValue(IMPLEMENTATION_VERSION);
        verifyNoMoreInteractions(classLoader, badManifest, badAttributes, goodManifest, goodAttributes);
        verifyNoInteractions(badUrl, goodUrl);
    }

    @Test
    public void throwsExceptionIfApplicationManifestContainsNoVersion() throws IOException {
        // given
        doReturn(classLoader).when(versionProvider).getClassLoader();
        when(classLoader.findResources(MANIFEST_FILE)).thenReturn(enumeration(list(badUrl, badUrl)));
        doReturn(badManifest).when(versionProvider).openManifest(badUrl);
        when(badManifest.getMainAttributes()).thenReturn(badAttributes);
        when(badAttributes.getValue(IMPLEMENTATION_TITLE)).thenReturn(TEST).thenReturn(APPLICATION_NAME);
        when(badAttributes.getValue(IMPLEMENTATION_VERSION)).thenReturn(null);

        // when
        Throwable thrown = catchThrowable(() -> versionProvider.getVersion());

        // then
        assertThat(thrown).isInstanceOf(IOException.class).hasMessage(NO_VERSION_ERROR_MESSAGE);
        verify(versionProvider).getClassLoader();
        verify(classLoader).findResources(MANIFEST_FILE);
        verify(versionProvider, times(2)).openManifest(badUrl);
        verify(badManifest, times(2)).getMainAttributes();
        verify(badAttributes, times(2)).getValue(IMPLEMENTATION_TITLE);
        verify(badAttributes).getValue(IMPLEMENTATION_VERSION);
        verifyNoMoreInteractions(classLoader, badManifest, badAttributes);
        verifyNoInteractions(badUrl);
    }

    @Test
    public void throwsExceptionIfNoVersionIsFound() throws IOException {
        // given
        doReturn(classLoader).when(versionProvider).getClassLoader();
        when(classLoader.findResources(MANIFEST_FILE)).thenReturn(enumeration(list(badUrl, badUrl)));
        doReturn(badManifest).when(versionProvider).openManifest(badUrl);
        when(badManifest.getMainAttributes()).thenReturn(badAttributes);
        when(badAttributes.getValue(IMPLEMENTATION_TITLE)).thenReturn(TEST);

        // when
        Throwable thrown = catchThrowable(() -> versionProvider.getVersion());

        // then
        assertThat(thrown).isInstanceOf(IOException.class).hasMessage(NO_VERSION_ERROR_MESSAGE);
        verify(versionProvider).getClassLoader();
        verify(classLoader).findResources(MANIFEST_FILE);
        verify(versionProvider, times(2)).openManifest(badUrl);
        verify(badManifest, times(2)).getMainAttributes();
        verify(badAttributes, times(2)).getValue(IMPLEMENTATION_TITLE);
        verifyNoMoreInteractions(classLoader, badManifest, badAttributes);
        verifyNoInteractions(badUrl);
    }

    @Test
    public void getAllManifestUrlsExceptionIsTranslated() throws IOException {
        // given
        doReturn(classLoader).when(versionProvider).getClassLoader();
        when(classLoader.findResources(MANIFEST_FILE)).thenThrow(IOException.class);

        // when
        Throwable thrown = catchThrowable(() -> versionProvider.getVersion());

        // then
        assertThat(thrown).isInstanceOf(IOException.class).hasMessage(ERROR_OCCURRED_MESSAGE);
        verify(versionProvider).getClassLoader();
        verify(classLoader).findResources(MANIFEST_FILE);
        verifyNoMoreInteractions(classLoader);
    }

    @Test
    public void openManifestExceptionIsTranslated() throws IOException {
        // given
        doReturn(classLoader).when(versionProvider).getClassLoader();
        when(classLoader.findResources(MANIFEST_FILE)).thenReturn(urls);
        when(badUrl.openStream()).thenThrow(IOException.class);

        // when
        Throwable thrown = catchThrowable(() -> versionProvider.getVersion());

        // then
        assertThat(thrown).isInstanceOf(IOException.class).hasMessage(ERROR_OCCURRED_MESSAGE);
        verify(versionProvider).getClassLoader();
        verify(classLoader).findResources(MANIFEST_FILE);
        verify(badUrl).openStream();
        verifyNoMoreInteractions(classLoader, badUrl);
        verifyNoInteractions(goodUrl);
    }
}
