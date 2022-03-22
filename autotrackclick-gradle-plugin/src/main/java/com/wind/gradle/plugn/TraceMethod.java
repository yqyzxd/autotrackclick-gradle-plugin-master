package com.wind.gradle.plugn;


import org.objectweb.asm.Opcodes;

public class TraceMethod {

    public int id;
    public int accessFlag;
    public String className;
    public String methodName;
    public String descriptor;

    public static TraceMethod create(int id,int accessFlag,String className,String methodName,String descriptor){
        TraceMethod method=new TraceMethod();
        method.id=id;
        method.accessFlag=accessFlag;
        method.className=className;
        method.methodName=methodName;
        method.descriptor=descriptor;
        return method;
    }

    public String getMethodName(){
        if (descriptor==null || isNativeMethod()){
            return this.className+"."+this.methodName;
        }else {
            return this.className+"."+this.methodName+"."+descriptor;
        }
    }


    public boolean isNativeMethod(){
        return (accessFlag & Opcodes.ACC_NATIVE) !=0;
    }
}
