package com.example.agent;

import com.example.interceptor.AgentConfig;
import com.example.interceptor.InterceptedOutputStream;
import net.bytebuddy.asm.Advice;

import java.io.OutputStream;

public class GetOutputStreamAdvice {
    @Advice.OnMethodExit
    public static void onExit(
            @Advice.Return(readOnly = false) OutputStream returnedStream
    ) {
        // 원래 Socket.getOutputStream()이 만들어낸 스트림을 InterceptedOutputStream으로 교체
        returnedStream = new InterceptedOutputStream(returnedStream, AgentConfig.getLogFilePath());
    }
}
