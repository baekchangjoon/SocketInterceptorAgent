package com.example.agent;

import com.example.app.PullOut;
import com.example.interceptor.AgentConfig;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.utility.JavaModule;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.Socket;
import java.util.jar.JarFile;

import static java.lang.System.exit;
import static net.bytebuddy.matcher.ElementMatchers.*;

public class SocketAgent {

    public static void premain(String agentArgs, Instrumentation inst) {
        if (AgentConfig.parse(agentArgs) == null) {
            System.out.println("[Agent] premain() invoked. Args: " + agentArgs);
            exit(1);
        }
        System.out.println("[Agent] Jar file path: " + AgentConfig.getJarFilePath());
        System.out.println("[Agent] Log file path: " + AgentConfig.getLogFilePath());
        System.out.println("[Agent] PullOut classLoader: " + PullOut.class.getClassLoader());

        appendToBootstrap(inst);

        AgentBuilder.Listener customListener = new AgentBuilder.Listener() {
            @Override
            public void onDiscovery(String typeName, ClassLoader classLoader,
                                    JavaModule module, boolean loaded) {
                System.out.println("Discovery: " + typeName + ", loaded=" + loaded);
            }

            @Override
            public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader,
                                         JavaModule module, boolean loaded, DynamicType dynamicType) {
                System.out.println("Transformed: " + typeDescription.getName());
            }

            @Override
            public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader,
                                  JavaModule module, boolean loaded) {
                System.out.println("Ignored: " + typeDescription.getName());
            }

            @Override
            public void onError(String typeName, ClassLoader classLoader,
                                JavaModule module, boolean loaded, Throwable throwable) {
                System.out.println("Error on: " + typeName + ", " + throwable);
            }

            @Override
            public void onComplete(String typeName, ClassLoader classLoader,
                                   JavaModule module, boolean loaded) {
                System.out.println("Complete: " + typeName);
            }
        };

        System.out.println("[Agent] isRetransformClassesSupported=" + inst.isRetransformClassesSupported());
        System.out.println("[Agent] PollOut, isModifiableClass=" + inst.isModifiableClass(PullOut.class));
        System.out.println("[Agent] BufferedReader, isModifiableClass=" + inst.isModifiableClass(BufferedReader.class));
        System.out.println("[Agent] Socket, isModifiableClass=" + inst.isModifiableClass(Socket.class));

        AgentBuilder rabbitBuilder = new AgentBuilder.Default()
                .ignore(none())
                .type(named("com.example.app.PullOut"))
                .transform((builder, typeDescription, classLoader, javaModule) ->
                        builder.method(named("pullOut")).intercept(FixedValue.value("Intercepted Rabbit!")));
        rabbitBuilder.installOn(inst);

        AgentBuilder bufferdReaderBuilder = new AgentBuilder.Default()
                .ignore(none())
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .type(named("java.io.BufferedReader"))
                .transform((builder, typeDescription, classLoader, javaModule) ->
                        builder.visit(Advice.to(BufferedReaderAdvice.class).on(named("readLine")))
                );
        bufferdReaderBuilder.installOn(inst);

        AgentBuilder socketBuilder = new AgentBuilder.Default()
                .ignore(none())
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .type(named("java.net.Socket"))
                .transform((builder, typeDescription, classLoader, module) ->
                        builder
                            .visit(Advice.to(GetInputStreamAdvice.class).on(named("getInputStream")))
                            .visit(Advice.to(GetOutputStreamAdvice.class).on(named("getOutputStream")))
                );
        socketBuilder.installOn(inst);
    }

    private static void appendToBootstrap(Instrumentation inst) {
        try {
            inst.appendToBootstrapClassLoaderSearch(new JarFile(AgentConfig.getJarFilePath()));
        } catch (IOException e) {
            System.out.println("[Agent] Failed to load jar file: " + AgentConfig.getJarFilePath() + ", " + e.getMessage());
            exit(1);
        }
    }
}
