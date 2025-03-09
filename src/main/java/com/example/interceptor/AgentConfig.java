package com.example.interceptor;

import java.io.File;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.util.HashMap;
import java.util.Map;

public class AgentConfig {

    public static Map<String, String> parse(String agentArgs) {
        if (agentArgs == null || agentArgs.isEmpty()) {
            return null;
        }
        Map<String, String> configMap = new HashMap<>();
        String[] tokens = agentArgs.split(",");
        for (String token : tokens) {
            String[] kv = token.split("=");
            if (kv.length == 2) {
                configMap.put(kv[0].trim(), kv[1].trim());
            }
        }
        if (configMap.containsKey("logFile")) {
            String logFilePath = configMap.get("logFile");
            System.setProperty("logFilePath", logFilePath);
        }
        if (configMap.containsKey("jarFile")) {
            String jarFilePath = configMap.get("jarFile");
            System.setProperty("jarFilePath", jarFilePath);
        }
        return configMap;
    }

    public static String getLogFilePath() {
        return System.getProperty("logFilePath");
    }

    public static String getJarFilePath() {
        return System.getProperty("jarFilePath");
    }
}
