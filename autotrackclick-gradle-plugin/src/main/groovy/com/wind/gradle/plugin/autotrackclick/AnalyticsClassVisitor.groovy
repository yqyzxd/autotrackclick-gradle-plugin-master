package com.wind.gradle.plugin.autotrackclick


import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class AnalyticsClassVisitor extends ClassVisitor{

    private String[] mInterfaces
    AnalyticsClassVisitor( ClassVisitor classVisitor) {
        super(Opcodes.ASM7, classVisitor)
    }

    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces)
        mInterfaces=interfaces
    }



    @Override
    MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor methodVisitor= super.visitMethod(access, name, descriptor, signature, exceptions)

        methodVisitor=new AnalyticsMethodVisitor(methodVisitor,access,name,descriptor)

        return methodVisitor
    }
}