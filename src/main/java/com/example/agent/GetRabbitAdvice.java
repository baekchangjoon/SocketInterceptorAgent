package com.example.agent;

import com.example.interceptor.AgentConfig;
import com.example.interceptor.InterceptedInputStream;
import net.bytebuddy.asm.Advice;

import java.io.InputStream;

public class GetRabbitAdvice {

    @Advice.OnMethodEnter
    public static void interceptor() {
        System.out.println("hello springboot, i know you will print the starting info");
    }
    /**
     * 메서드 실행이 끝난 후(@OnMethodExit) 반환값(@Advice.Return)을 가로채서 다른 객체로 교체
     */
    @Advice.OnMethodExit
    public static void onExit(
            @Advice.Return(readOnly = false) InputStream returnedStream
    ) {
        // 원래 Socket.getInputStream()이 만들어낸 스트림이 returnedStream에 들어있음
        // 이것을 InterceptedInputStream으로 교체
        returnedStream = new InterceptedInputStream(returnedStream, AgentConfig.getLogFilePath());
    }
}
