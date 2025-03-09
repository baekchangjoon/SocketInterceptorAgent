package com.example.agent;

import net.bytebuddy.asm.Advice;

public class BufferedReaderAdvice {

    @Advice.OnMethodExit
    public static void onExit(
        @Advice.Return(readOnly = false) String result
    ) {
        result = "Rabbit!";
    }
}
