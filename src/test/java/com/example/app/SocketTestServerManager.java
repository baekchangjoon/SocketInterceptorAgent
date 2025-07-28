package com.example.app;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class SocketTestServerManager {
    private static Thread serverThread;
    private static boolean startedByTest = false;

    @BeforeAll
    public static void startServerIfNotRunning() throws Exception {
        if (!isServerRunning()) {
            startedByTest = true;
            serverThread = new Thread(() -> {
                try {
                    MyEchoServer.main(new String[]{});
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            serverThread.setDaemon(true);
            serverThread.start();
            
            // 서버가 실제로 준비될 때까지 대기 (최대 5초)
            int maxAttempts = 50;
            int attempts = 0;
            while (!isServerRunning() && attempts < maxAttempts) {
                Thread.sleep(100);
                attempts++;
            }
            
            if (!isServerRunning()) {
                throw new RuntimeException("서버가 시작되지 않았습니다.");
            }
        }
    }

    @AfterAll
    public static void stopServer() throws Exception {
        if (startedByTest) {
            try (Socket socket = new Socket("localhost", 12345);
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
                writer.write("SHUTDOWN\n");
                writer.flush();
            } catch (IOException e) {
                // 이미 종료되었거나 연결 불가
            }
            // 서버 종료 대기
            Thread.sleep(1000);
        }
    }

    private static boolean isServerRunning() {
        try (Socket socket = new Socket("localhost", 12345)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
} 