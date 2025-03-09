package com.example.interceptor;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

public class RecordToFileUtil {
    /**
     * 단순히 synchronized로 동시성 제어.
     */
    public static synchronized void writeLine(String filePath, String data) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath, true))) {
            pw.println(LocalDateTime.now() + " " + data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
