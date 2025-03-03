package com.example.agent;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;

public class SocketInterceptorAgent {
    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("[Agent] premain() invoked. Args: " + agentArgs);

        // 에이전트 인자 파싱 (logFile=/path/to/log.txt 형식 가정)
        Map<String, String> config = parseAgentArgs(agentArgs);

        // JAR 를 bootstrap classloader search path에 추가
        try {
            inst.appendToBootstrapClassLoaderSearch(new JarFile("/Users/changjoonbaek/github_socketInterceptor/SocketInterceptorAgent/target/SocketInterceptorAgent-1.0-SNAPSHOT.jar"));
            // 이제 Bootstrap ClassLoader에서도 InterceptedXXX 클래스를 볼 수 있음
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Transformer 등록
        inst.addTransformer(new SocketTransformer(config), true);
    }

    // 간단한 인자 파싱
    private static Map<String, String> parseAgentArgs(String agentArgs) {
        Map<String, String> configMap = new HashMap<>();
        if (agentArgs == null || agentArgs.isEmpty()) {
            return configMap;
        }
        String[] tokens = agentArgs.split(",");
        for (String token : tokens) {
            String[] kv = token.split("=");
            if (kv.length == 2) {
                configMap.put(kv[0].trim(), kv[1].trim());
            }
        }
        return configMap;
    }
}
