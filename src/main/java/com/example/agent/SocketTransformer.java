package com.example.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Map;
import javassist.*;

public class SocketTransformer implements ClassFileTransformer {

    private final String logFilePath;

    public SocketTransformer(Map<String, String> config) {
        // 설정 값에서 logFile 가져오기
        this.logFilePath = config.getOrDefault("logFile", "socket_log.txt");
    }

    @Override
    public byte[] transform(ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {
        // java.net.Socket 클래스만 변환
        if (!"java/net/Socket".equals(className)) {
            return classfileBuffer;
        }
        try {
            ClassPool cp = ClassPool.getDefault();
            CtClass ctClass = cp.makeClass(new java.io.ByteArrayInputStream(classfileBuffer));
            if (ctClass.isInterface()) {
                return classfileBuffer;
            }
            // getInputStream() 메서드 후킹
            CtMethod getIS = ctClass.getDeclaredMethod("getInputStream");
            getIS.setBody(
                "{ " +
                "  java.io.InputStream original = ($w)$_super_getInputStream(); " + // 원래 로직 호출
                "  return new com.example.agent.InterceptedInputStream(original, \"" + logFilePath + "\"); " +
                "}"
                .replace("_super_", "super.") // setBody 안에서 super 호출은 불가하므로 치환
            );

            // getOutputStream() 메서드 후킹
            CtMethod getOS = ctClass.getDeclaredMethod("getOutputStream");
            getOS.setBody(
                "{ " +
                "  java.io.OutputStream original = ($w)$_super_getOutputStream(); " +
                "  return new com.example.agent.InterceptedOutputStream(original, \"" + logFilePath + "\"); " +
                "}"
                .replace("_super_", "super.")
            );

            byte[] byteCode = ctClass.toBytecode();
            ctClass.detach();
            System.out.println("[Agent] Successfully transformed java.net.Socket");
            return byteCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classfileBuffer;
    }
}
