package com.lyc.easyreader.bookshelf.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class ByteCountingLineReader implements Closeable {
    private final Charset charset;
    private InputStream in;
    private long _byteCount;
    private int bufferedByte = -1;
    private boolean ended;

    // in should be a buffered stream!
    public ByteCountingLineReader(InputStream in, Charset charset) {
        this.in = in;
        this.charset = charset;
    }

    public ByteCountingLineReader(File f, Charset charset) throws IOException {
        this(new BufferedInputStream(new FileInputStream(f), 65536), charset);
    }

    public ByteCountingLineReader(FileDescriptor fd, Charset charset) {
        this(new BufferedInputStream(new FileInputStream(fd), 65536), charset);
    }

    public String readLine() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(65536);
        if (ended) return null;
        while (true) {
            int c = read();
            if (ended && baos.size() == 0) return null;
            if (ended || c == '\n') break;
            if (c == '\r') {
                c = read();
                if (c != '\n' && !ended)
                    bufferedByte = c;
                break;
            }
            baos.write(c);
        }
        return new String(baos.toByteArray(), charset);
    }

    private int read() throws IOException {
        if (bufferedByte >= 0) {
            int b = bufferedByte;
            bufferedByte = -1;
            return b;
        }
        int c = in.read();
        if (c < 0) ended = true;
        else ++_byteCount;
        return c;
    }

    public long byteCount() {
        return bufferedByte >= 0 ? _byteCount - 1 : _byteCount;
    }

    public void close() throws IOException {
        if (in != null) {
            try {
                in.close();
            } finally {
                in = null;
            }
        }
    }

    public boolean ended() {
        return ended;
    }

}
