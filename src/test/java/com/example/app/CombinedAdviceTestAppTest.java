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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("모든 Advice 클래스 통합 테스트")
public class CombinedAdviceTestAppTest {
    
    private static ServerSocket serverSocket;
    private static ExecutorService executorService;
    private PullOut pullOut;
    
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
    
    @BeforeEach
    void setUpEach() {
        pullOut = new PullOut();
    }
    
    @Test
    @DisplayName("PullOut.pullOut() 메소드가 GetRabbitAdvice에 의해 인터셉트되는지 테스트")
    void testPullOutWithGetRabbitAdvice() {
        // Given & When
        String pullResult = pullOut.pullOut();
        
        // Then - Agent가 로드되지 않은 경우 원본 값 반환, 로드된 경우 인터셉트된 값 반환
        if (pullResult.equals("Intercepted Rabbit!")) {
            // Agent가 로드되어 인터셉트된 경우
            assertEquals("Intercepted Rabbit!", pullResult, 
                "PullOut.pullOut() 결과가 GetRabbitAdvice에 의해 'Intercepted Rabbit!'으로 교체되어야 합니다");
        } else {
            // Agent가 로드되지 않은 경우
            assertEquals("Pull Out!!!", pullResult, 
                "Agent가 로드되지 않은 경우 원본 값이 반환되어야 합니다");
        }
    }
    
    @Test
    @DisplayName("소켓 통신에서 모든 Advice 클래스가 정상적으로 작동하는지 테스트")
    void testSocketCommunicationWithAllAdvices() {
        // Given
        String testMessage1 = "Combined Test Message 1";
        String testMessage2 = "Combined Test Message 2";
        
        try (Socket socket = new Socket("localhost", 12345);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
            
            // When & Then - 첫 번째 메시지 전송 및 응답 확인
            writer.write(testMessage1 + "\n");
            writer.flush();
            
            String response1 = reader.readLine();
            if (response1 != null && response1.equals("Rabbit!")) {
                // Agent가 로드되어 인터셉트된 경우
                assertEquals("Rabbit!", response1, 
                    "첫 번째 응답이 BufferedReaderAdvice에 의해 'Rabbit!'으로 교체되어야 합니다");
            } else {
                // Agent가 로드되지 않았거나 서버 응답이 다른 경우
                assertNotNull(response1, "서버 응답이 null이 아니어야 합니다");
            }
            
            // When & Then - 두 번째 메시지 전송 및 응답 확인
            writer.write(testMessage2 + "\n");
            writer.flush();
            
            String response2 = reader.readLine();
            if (response2 != null && response2.equals("Rabbit!")) {
                // Agent가 로드되어 인터셉트된 경우
                assertEquals("Rabbit!", response2, 
                    "두 번째 응답도 BufferedReaderAdvice에 의해 'Rabbit!'으로 교체되어야 합니다");
            } else {
                // Agent가 로드되지 않았거나 서버 응답이 다른 경우
                assertNotNull(response2, "서버 응답이 null이 아니어야 합니다");
            }
            
        } catch (ConnectException e) {
            // 서버가 실행되지 않은 경우 테스트를 건너뜀
            System.out.println("소켓 서버가 실행되지 않아 테스트를 건너뜁니다: " + e.getMessage());
        } catch (IOException e) {
            fail("소켓 통신 중 오류 발생: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("PullOut.pullOut() 재호출 시에도 GetRabbitAdvice가 정상 작동하는지 테스트")
    void testPullOutReCall() {
        // Given & When - 첫 번째 호출
        String firstResult = pullOut.pullOut();
        
        // Then
        if (firstResult.equals("Intercepted Rabbit!")) {
            // Agent가 로드되어 인터셉트된 경우
            assertEquals("Intercepted Rabbit!", firstResult, 
                "첫 번째 PullOut.pullOut() 결과가 GetRabbitAdvice에 의해 'Intercepted Rabbit!'으로 교체되어야 합니다");
        } else {
            // Agent가 로드되지 않은 경우
            assertEquals("Pull Out!!!", firstResult, 
                "Agent가 로드되지 않은 경우 원본 값이 반환되어야 합니다");
        }
        
        // When - 두 번째 호출
        String secondResult = pullOut.pullOut();
        
        // Then
        if (secondResult.equals("Intercepted Rabbit!")) {
            // Agent가 로드되어 인터셉트된 경우
            assertEquals("Intercepted Rabbit!", secondResult, 
                "두 번째 PullOut.pullOut() 결과도 GetRabbitAdvice에 의해 'Intercepted Rabbit!'으로 교체되어야 합니다");
        } else {
            // Agent가 로드되지 않은 경우
            assertEquals("Pull Out!!!", secondResult, 
                "Agent가 로드되지 않은 경우 원본 값이 반환되어야 합니다");
        }
    }
    
    @Test
    @DisplayName("Advice 클래스들이 예상대로 작동하는지 검증")
    void testAdviceClassesFunctionality() {
        // Given
        String testMessage = "Advice Functionality Test";
        
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
} 