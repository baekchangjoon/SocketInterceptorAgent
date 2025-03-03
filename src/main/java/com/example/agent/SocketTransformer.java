package com.example.agent;

import javassist.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Map;

public class SocketTransformer implements ClassFileTransformer {

    private final String logFilePath;

    public SocketTransformer(Map<String, String> config) {
        this.logFilePath = config.getOrDefault("logFile", "socket_log.txt");
        System.out.println("[Agent] Socket Log File: " + logFilePath);
    }

    @Override
    public byte[] transform(ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {

        if (!"java/net/Socket".equals(className)) {
            return classfileBuffer;
        }
//        if (!className.contains("java/net/Socket")) {
//            return classfileBuffer;
//        }
        System.out.println("[Agent] className: " + className);

        try {
//            ClassPool cp = ClassPool.getDefault();
//            CtClass ctClass = cp.makeClass(new java.io.ByteArrayInputStream(classfileBuffer));

            ClassPool cp = new ClassPool(true);
            cp.appendClassPath(new LoaderClassPath(loader));
            String dottedName = className.replace('/', '.');
            CtClass ctClass = cp.makeClass(new java.io.ByteArrayInputStream(classfileBuffer));
            System.out.println("[Agent] dottedName: " + dottedName);

            if (ctClass.isInterface()) {
                return classfileBuffer;
            }

            // -----------------------------
            // 1) getInputStream() 후킹
            // -----------------------------
            try {
                CtMethod getIS = ctClass.getDeclaredMethod("getInputStream");
                // 원본 메서드를 다른 이름으로 변경
                String oldMethodName = "getInputStream$original";
                getIS.setName(oldMethodName);

                // 새 메서드 작성
                // 원본 메서드를 호출한 뒤, 반환값을 InterceptedInputStream으로 감싸서 리턴
                CtMethod newGetIS = CtNewMethod.make(
                        "public java.io.InputStream getInputStream() throws java.io.IOException {"
                                + "    java.io.InputStream original = this." + oldMethodName + "();"
                                + "    return new com.example.agent.InterceptedInputStream(original, \"" + logFilePath + "\");"
                                + "}",
                        ctClass
                );
                ctClass.addMethod(newGetIS);

            } catch (NotFoundException e) {
                System.out.println("[Agent] getInputStream() not found, skip hooking.");
            }

            // -----------------------------
            // 2) getOutputStream() 후킹
            // -----------------------------
            try {
                CtMethod getOS = ctClass.getDeclaredMethod("getOutputStream");
                // 원본 메서드 리네이밍
                String oldMethodName = "getOutputStream$original";
                getOS.setName(oldMethodName);

                // 새 메서드 작성
                CtMethod newGetOS = CtNewMethod.make(
                        "public java.io.OutputStream getOutputStream() throws java.io.IOException {"
                                + "    java.io.OutputStream original = this." + oldMethodName + "();"
                                + "    return new com.example.agent.InterceptedOutputStream(original, \"" + logFilePath + "\");"
                                + "}",
                        ctClass
                );
                ctClass.addMethod(newGetOS);

            } catch (NotFoundException e) {
                System.out.println("[Agent] getOutputStream() not found, skip hooking.");
            }

            // 최종 변환된 바이트코드 반환
            byte[] byteCode = ctClass.toBytecode();
            ctClass.detach();

            System.out.println("[Agent] Successfully transformed java.net.Socket");
            return byteCode;

        } catch (Exception e) {
            System.out.println("[Agent] Exception, " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("[Agent] Here?");
        return classfileBuffer;
    }
}
