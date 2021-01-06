package com.wind.gradle.plugin.autotrackclick

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.commons.AdviceAdapter


class AnalyticsMethodVisitor extends AdviceAdapter{


    /**
     * Constructs a new {@link AdviceAdapter}.
     *
     * @param api the ASM API version implemented by this visitor. Must be one of {@link
     *     Opcodes#ASM4}, {@link Opcodes#ASM5}, {@link Opcodes#ASM6} or {@link Opcodes#ASM7}.
     * @param methodVisitor the method visitor to which this adapter delegates calls.
     * @param access the method's access flags (see {@link Opcodes}).
     * @param name the method's name.
     * @param descriptor the method's descriptor (see {@link Type Type}).
     */
    protected AnalyticsMethodVisitor(MethodVisitor methodVisitor, int access, String name, String descriptor) {
        super(Opcodes.ASM6, methodVisitor, access, name, descriptor)
    }
}