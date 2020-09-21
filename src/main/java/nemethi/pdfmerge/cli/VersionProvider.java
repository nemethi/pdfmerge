package nemethi.pdfmerge.cli;

import picocli.CommandLine.IVersionProvider;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.concurrent.Callable;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import static java.util.jar.Attributes.Name.IMPLEMENTATION_TITLE;
import static java.util.jar.Attributes.Name.IMPLEMENTATION_VERSION;

public class VersionProvider implements IVersionProvider {

    private static final String APPLICATION_NAME = "pdfmerge";
    private static final String MANIFEST_FILE = "META-INF/MANIFEST.MF";
    private static final String ERROR_OCCURRED_MESSAGE = "An error occurred during reading the version.";
    private static final String NO_VERSION_ERROR_MESSAGE = "Could not find version information.";

    @Override
    public String[] getVersion() throws Exception {
        Enumeration<URL> manifestUrls = getAllManifestUrls(getClassLoader());
        String version = null;
        while (version == null && manifestUrls.hasMoreElements()) {
            URL url = manifestUrls.nextElement();
            version = readManifest(openManifest(url));
        }
        if (version == null) {
            throw new IOException(NO_VERSION_ERROR_MESSAGE);
        }
        return new String[]{version};
    }

    ClassLoader getClassLoader() throws IOException {
        return valueWithErrorTranslation(() -> getClass().getClassLoader());
    }

    private Enumeration<URL> getAllManifestUrls(ClassLoader classLoader) throws IOException {
        return valueWithErrorTranslation(() -> classLoader.getResources(MANIFEST_FILE));
    }

    Manifest openManifest(URL url) throws IOException {
        return valueWithErrorTranslation(() -> new Manifest(url.openStream()));
    }

    private String readManifest(Manifest manifest) throws IOException {
        Attributes attributes = manifest.getMainAttributes();
        if (isApplicationManifest(attributes)) {
            return getVersion(attributes);
        }
        return null;
    }

    private boolean isApplicationManifest(Attributes attributes) {
        return APPLICATION_NAME.equals(attributes.getValue(IMPLEMENTATION_TITLE));
    }

    private String getVersion(Attributes attributes) throws IOException {
        String version = attributes.getValue(IMPLEMENTATION_VERSION);
        if (version != null) {
            return version;
        }
        throw new IOException(NO_VERSION_ERROR_MESSAGE);
    }

    private <V> V valueWithErrorTranslation(Callable<V> callable) throws IOException {
        try {
            return callable.call();
        } catch (Exception e) {
            throw new IOException(ERROR_OCCURRED_MESSAGE);
        }
    }
}
