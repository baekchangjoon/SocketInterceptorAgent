package com.example.interceptor;

import java.io.IOException;
import java.io.OutputStream;

public class InterceptedOutputStream extends OutputStream {

    private final OutputStream original;
    private final String logFile;

    public InterceptedOutputStream(OutputStream original, String logFile) {
        this.original = original;
        this.logFile = logFile;
    }

    @Override
    public void write(int b) throws IOException {
        RecordToFileUtil.writeLine(logFile, "[SEND_BYTE] " + b);
        original.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (len > 0) {
            byte[] chunk = new byte[len];
            System.arraycopy(b, off, chunk, 0, len);
            RecordToFileUtil.writeLine(logFile, "[SEND_DATA] " + new String(chunk));
        }
        original.write(b, off, len);
    }
}
