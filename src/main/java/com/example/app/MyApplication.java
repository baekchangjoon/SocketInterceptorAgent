package com.example.app;

import java.io.*;
import java.net.Socket;

public class MyApplication {
    public static void main(String[] args) {
        System.out.println("[MyApplication] Connecting to localhost:12345");
        try (Socket socket = new Socket("localhost", 12345);
             BufferedReader reader =
                 new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter writer =
                 new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            writer.write("Hello Server\n");
            writer.flush();
            System.out.println("[MyApplication] Sent: Hello Server");

            String resp = reader.readLine();
            System.out.println("[MyApplication] Received: " + resp);

            writer.write("Another Message\n");
            writer.flush();
            System.out.println("[MyApplication] Sent: Another Message");

            resp = reader.readLine();
            System.out.println("[MyApplication] Received: " + resp);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
