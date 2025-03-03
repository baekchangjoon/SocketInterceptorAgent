package com.example.agent;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

public class RecordToFileUtil {
    public static synchronized void writeLine(String filePath, String data) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath, true))) {
            pw.println(LocalDateTime.now() + " " + data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
