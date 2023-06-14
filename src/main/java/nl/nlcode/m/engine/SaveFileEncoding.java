package nl.nlcode.m.engine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author leo
 */
public enum SaveFileEncoding {

    RAW,
    GZIP;

    public String toDesc() {
        return name().toLowerCase(Locale.UK);
    }

    public static SaveFileEncoding fromDesc(String desc) {
        return valueOf(desc.toUpperCase(Locale.UK));
    }

    public OutputStream wrap(OutputStream orig) throws IOException {
        return switch (this) {
            case GZIP ->
                new GZIPOutputStream(orig);
            case RAW ->
                orig;
            default ->
                throw new IllegalStateException();
        };
    }

    public InputStream wrap(InputStream orig) throws IOException {
        return switch (this) {
            case GZIP ->
                new GZIPInputStream(orig);
            case RAW ->
                orig;
            default ->
                throw new IllegalStateException();
        };
    }

}
