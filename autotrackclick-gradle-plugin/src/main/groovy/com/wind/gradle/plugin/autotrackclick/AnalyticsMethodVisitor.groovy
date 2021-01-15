package com.wind.gradle.plugin.autotrackclick

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Handle
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter


class AnalyticsMethodVisitor extends AdviceAdapter {
    String[] mInterfaces
    String mNameDesc
    protected AnalyticsMethodVisitor(String[] interfaces, MethodVisitor methodVisitor, int access, String name, String descriptor) {
        super(Opcodes.ASM7, methodVisitor, access, name, descriptor)
        mInterfaces = interfaces
        mNameDesc=name+ descriptor
    }

    /*@Override
    AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        if (descriptor == 'Lcom/wind/gradle/plugin/autotrackclick/ASMTime;'){
            isASMTimeAnnotation=true
        }
        return super.visitAnnotation(descriptor, visible)
    }*/


    @Override
    void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments)

        if (bootstrapMethodArguments != null) {
            for (int i = 0; i < bootstrapMethodArguments.length; i++) {
                println("visitInvokeDynamicInsn->" + bootstrapMethodArguments[i])
            }
        }


    }

    @Override
    protected void onMethodEnter() {
        super.onMethodEnter()


        if (mInterfaces != null && mInterfaces.length > 0) {

            if ((mInterfaces.contains('android/view/View$OnClickListener') && mNameDesc == 'onClick(Landroid/view/View;)V')){
                println("onMethodEnter->AutoAnalyticsHelper")
                visitVarInsn(ALOAD,1)//加载方法参数
                visitMethodInsn(INVOKESTATIC,"com/wind/analytics/AutoAnalyticsHelper","trackViewOnClick","(Landroid/view/View;)V",false)

            }

        }


    }

    @Override
    protected void onMethodExit(int opcode) {
        super.onMethodExit(opcode)
        /*if (isASMTimeAnnotation) {
            visitMethodInsn(INVOKESTATIC, "com/wind/analytics/ATime", "end", "()V", false)
        }*/
    }
}