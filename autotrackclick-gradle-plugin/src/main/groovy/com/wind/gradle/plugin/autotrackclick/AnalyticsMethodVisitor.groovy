package com.wind.gradle.plugin.autotrackclick

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter


class AnalyticsMethodVisitor extends AdviceAdapter{
    boolean isASMTimeAnnotation = false
    protected AnalyticsMethodVisitor(MethodVisitor methodVisitor, int access, String name, String descriptor) {
        super(Opcodes.ASM7, methodVisitor, access, name, descriptor)

    }

    @Override
    AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        if (descriptor == 'Lcom/wind/gradle/plugin/autotrackclick/ASMTime;'){
            isASMTimeAnnotation=true
        }
        return super.visitAnnotation(descriptor, visible)
    }

    @Override
    protected void onMethodEnter() {
        super.onMethodEnter()
       // println("AnalyticsMethodVisitor onMethodEnter isASMTimeAnnotation:"+isASMTimeAnnotation)

        if (isASMTimeAnnotation) {


            //java.lang.System.currentTimeMillis()
            /**
             * final int opcode,
             *       final String owner,
             *       final String name,
             *       final String descriptor,
             *       final boolean isInterface
             */
            //visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false)
            visitMethodInsn(INVOKESTATIC, "com/wind/analytics/ATime", "start", "()V", false)
        }





    }

    @Override
    protected void onMethodExit(int opcode) {
        super.onMethodExit(opcode)
        if (isASMTimeAnnotation) {
            visitMethodInsn(INVOKESTATIC, "com/wind/analytics/ATime", "end", "()V", false)
        }
    }
}