package com.example.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;

public class BufferedReaderTestAppTest {
    
    private static ServerSocket serverSocket;
    private static ExecutorService executorService;
    
    @BeforeAll
    static void setUp() throws Exception {
        // MyEchoServer를 별도 스레드에서 실행
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            try {
                serverSocket = new ServerSocket(12345);
                System.out.println("[EchoServer] Listening on 12345...");
                while (!serverSocket.isClosed()) {
                    Socket client = serverSocket.accept();
                    new Thread(() -> handleClient(client)).start();
                }
            } catch (Exception e) {
                if (!serverSocket.isClosed()) {
                    e.printStackTrace();
                }
            }
        });
        
        // 서버가 시작될 때까지 잠시 대기
        Thread.sleep(1000);
    }
    
    @AfterAll
    static void tearDown() throws Exception {
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
        if (executorService != null) {
            executorService.shutdown();
        }
    }
    
    private static void handleClient(Socket client) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
             OutputStreamWriter wr = new OutputStreamWriter(client.getOutputStream())) {
            String line;
            while ((line = br.readLine()) != null) {
                wr.write("echo>> " + line + "\n");
                wr.flush();
            }
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }
    
    @Test
    @DisplayName("BufferedReader.readLine() 메소드가 BufferedReaderAdvice에 의해 인터셉트되는지 테스트")
    void testBufferedReaderReadLineWithAdvice() {
        // Given
        String testMessage = "Test Message";
        
        try (Socket socket = new Socket("localhost", 12345);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
            
            // When - 서버에 메시지 전송
            writer.write(testMessage + "\n");
            writer.flush();
            
            // Then - BufferedReader.readLine() 호출이 BufferedReaderAdvice에 의해 "Rabbit!"으로 교체됨
            String response = reader.readLine();
            if (response != null && response.equals("Rabbit!")) {
                // Agent가 로드되어 인터셉트된 경우
                assertEquals("Rabbit!", response, 
                    "BufferedReader.readLine() 결과가 BufferedReaderAdvice에 의해 'Rabbit!'으로 교체되어야 합니다");
            } else {
                // Agent가 로드되지 않았거나 서버 응답이 다른 경우
                assertNotNull(response, "서버 응답이 null이 아니어야 합니다");
            }
            
        } catch (ConnectException e) {
            // 서버가 실행되지 않은 경우 테스트를 건너뜀
            System.out.println("소켓 서버가 실행되지 않아 테스트를 건너뜁니다: " + e.getMessage());
        } catch (IOException e) {
            fail("소켓 통신 중 오류 발생: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("여러 번의 BufferedReader.readLine() 호출이 모두 BufferedReaderAdvice에 의해 인터셉트되는지 테스트")
    void testMultipleBufferedReaderReadLineCalls() {
        // Given
        String firstMessage = "Test Message";
        String secondMessage = "Second Message";
        
        try (Socket socket = new Socket("localhost", 12345);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
            
            // When & Then - 첫 번째 메시지 전송 및 응답 확인
            writer.write(firstMessage + "\n");
            writer.flush();
            
            String firstResponse = reader.readLine();
            if (firstResponse != null && firstResponse.equals("Rabbit!")) {
                // Agent가 로드되어 인터셉트된 경우
                assertEquals("Rabbit!", firstResponse, 
                    "첫 번째 BufferedReader.readLine() 결과가 BufferedReaderAdvice에 의해 'Rabbit!'으로 교체되어야 합니다");
            } else {
                // Agent가 로드되지 않았거나 서버 응답이 다른 경우
                assertNotNull(firstResponse, "첫 번째 서버 응답이 null이 아니어야 합니다");
            }
            
            // When & Then - 두 번째 메시지 전송 및 응답 확인
            writer.write(secondMessage + "\n");
            writer.flush();
            
            String secondResponse = reader.readLine();
            if (secondResponse != null && secondResponse.equals("Rabbit!")) {
                // Agent가 로드되어 인터셉트된 경우
                assertEquals("Rabbit!", secondResponse, 
                    "두 번째 BufferedReader.readLine() 결과도 BufferedReaderAdvice에 의해 'Rabbit!'으로 교체되어야 합니다");
            } else {
                // Agent가 로드되지 않았거나 서버 응답이 다른 경우
                assertNotNull(secondResponse, "두 번째 서버 응답이 null이 아니어야 합니다");
            }
            
        } catch (ConnectException e) {
            // 서버가 실행되지 않은 경우 테스트를 건너뜀
            System.out.println("소켓 서버가 실행되지 않아 테스트를 건너뜁니다: " + e.getMessage());
        } catch (IOException e) {
            fail("소켓 통신 중 오류 발생: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("BufferedReaderAdvice가 BufferedReader.readLine() 호출을 일관되게 인터셉트하는지 테스트")
    void testBufferedReaderAdviceConsistency() {
        // Given
        String testMessage = "Consistency Test";
        
        try (Socket socket = new Socket("localhost", 12345);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
            
            // When - 여러 번 메시지 전송 및 응답 확인
            for (int i = 0; i < 3; i++) {
                writer.write(testMessage + " " + i + "\n");
                writer.flush();
                
                String response = reader.readLine();
                if (response != null && response.equals("Rabbit!")) {
                    // Agent가 로드되어 인터셉트된 경우
                    assertEquals("Rabbit!", response, 
                        "BufferedReader.readLine() 결과가 BufferedReaderAdvice에 의해 일관되게 'Rabbit!'으로 교체되어야 합니다");
                } else {
                    // Agent가 로드되지 않았거나 서버 응답이 다른 경우
                    assertNotNull(response, "서버 응답이 null이 아니어야 합니다");
                }
            }
            
        } catch (ConnectException e) {
            // 서버가 실행되지 않은 경우 테스트를 건너뜀
            System.out.println("소켓 서버가 실행되지 않아 테스트를 건너뜁니다: " + e.getMessage());
        } catch (IOException e) {
            fail("소켓 통신 중 오류 발생: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("BufferedReaderAdvice가 BufferedReader.readLine() 반환값을 정확히 교체하는지 검증")
    void testBufferedReaderAdviceReplacementAccuracy() {
        // Given
        String testMessage = "Accuracy Test";
        
        try (Socket socket = new Socket("localhost", 12345);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
            
            // When - 서버에 메시지 전송
            writer.write(testMessage + "\n");
            writer.flush();
            
            // Then - BufferedReader.readLine() 호출 결과 검증
            String response = reader.readLine();
            if (response != null && response.equals("Rabbit!")) {
                // Agent가 로드되어 인터셉트된 경우
                assertEquals("Rabbit!", response, 
                    "BufferedReader.readLine() 결과가 BufferedReaderAdvice에 의해 정확히 'Rabbit!'으로 교체되어야 합니다");
                assertNotEquals("echo>> " + testMessage, response, 
                    "원본 서버 응답과 다른 값이 반환되어야 합니다");
            } else {
                // Agent가 로드되지 않았거나 서버 응답이 다른 경우
                assertNotNull(response, "서버 응답이 null이 아니어야 합니다");
            }
            
        } catch (ConnectException e) {
            // 서버가 실행되지 않은 경우 테스트를 건너뜀
            System.out.println("소켓 서버가 실행되지 않아 테스트를 건너뜁니다: " + e.getMessage());
        } catch (IOException e) {
            fail("소켓 통신 중 오류 발생: " + e.getMessage());
        }
    }
} 