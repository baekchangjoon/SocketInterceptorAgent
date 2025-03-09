package com.example.app;

import java.io.*;
import java.net.Socket;

public class MyApplication {
    public static void main(String[] args) {
        PullOut pull = new PullOut();
        System.out.println("[MyApplication] Connecting to localhost:12345");
        System.out.println(pull.pullOut());
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
            System.out.println(pull.pullOut());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
