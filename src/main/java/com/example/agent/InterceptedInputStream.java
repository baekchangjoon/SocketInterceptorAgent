package com.example.agent;

import java.io.IOException;
import java.io.InputStream;

public class InterceptedInputStream extends InputStream {

    private final InputStream original;
    private final String logFile;

    public InterceptedInputStream(InputStream original, String logFile) {
        this.original = original;
        this.logFile = logFile;
    }

    @Override
    public int read() throws IOException {
        int data = original.read();
        if (data != -1) {
            RecordToFileUtil.writeLine(logFile, "[RECV_BYTE] " + data);
        }
        return data;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int result = original.read(b, off, len);
        if (result > 0) {
            byte[] chunk = new byte[result];
            System.arraycopy(b, off, chunk, 0, result);
            RecordToFileUtil.writeLine(logFile, "[RECV_DATA] " + new String(chunk));
        }
        return result;
    }
}
