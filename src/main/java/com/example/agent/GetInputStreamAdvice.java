package com.example.agent;

import com.example.interceptor.AgentConfig;
import com.example.interceptor.InterceptedInputStream;
import net.bytebuddy.asm.Advice;

import java.io.InputStream;

/**
 * getInputStream() 메서드가 끝난 뒤, 반환값을 InterceptedInputStream으로 교체한다.
 */
public class GetInputStreamAdvice {
    @Advice.OnMethodExit
    public static InputStream onExit(@Advice.Return(readOnly = false) InputStream returnedStream) {
        returnedStream = new InterceptedInputStream(returnedStream, AgentConfig.getLogFilePath());
        return returnedStream;
    }
}
