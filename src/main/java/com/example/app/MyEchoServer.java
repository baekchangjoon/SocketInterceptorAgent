package com.example.app;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class MyEchoServer {
    public static void main(String[] args) throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("[EchoServer] Listening on 12345...");
            while (true) {
                Socket client = serverSocket.accept();
                new Thread(() -> handleClient(client)).start();
            }
        }
    }

    private static void handleClient(Socket client) {
        try (BufferedReader br =
                     new BufferedReader(new InputStreamReader(client.getInputStream()));
             OutputStreamWriter wr =
                     new OutputStreamWriter(client.getOutputStream())) {
            String line;
            while ((line = br.readLine()) != null) {
                wr.write("echo>> " + line + "\n");
                wr.flush();
            }
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }
}
