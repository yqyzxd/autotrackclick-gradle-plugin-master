package com.wind.gradle.plugin.autotrackclick

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor

class AnalyticsClassVisitor extends ClassVisitor{


    AnalyticsClassVisitor( ClassVisitor classVisitor) {
        super(Opcodes.ASM6, classVisitor)
    }

    @Override
    MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor methodVisitor= super.visitMethod(access, name, descriptor, signature, exceptions)

        methodVisitor=new AnalyticsMethodVisitor(methodVisitor,access,name,descriptor)

        return methodVisitor
    }
}