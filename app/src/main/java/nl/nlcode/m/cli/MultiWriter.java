package nl.nlcode.m.cli;

import java.io.IOException;
import java.io.Writer;

/**
 *
 * @author jq59bu
 */
public class MultiWriter extends Writer {

    private Writer[] writers;

    public MultiWriter(Writer[] writers) {
        this.writers = writers;
    }

    @Override
    public void write(int c) throws IOException {
        for (Writer writer : writers) {
            writer.write(c);
        }
    }

    @Override
    public void flush() throws IOException {
        for (Writer writer : writers) {
            writer.flush();
        }
    }

    @Override
    public void close() throws IOException {
        for (Writer writer : writers) {
            writer.close();
        }
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        for (Writer writer : writers) {
            writer.write(cbuf, off, len);
        }
    }
}
