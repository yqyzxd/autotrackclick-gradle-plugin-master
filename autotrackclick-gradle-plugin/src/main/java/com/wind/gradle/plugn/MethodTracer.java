package com.wind.gradle.plugn;

import static org.codehaus.groovy.runtime.DefaultGroovyMethods.println;

import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class MethodTracer {

    private TraceBuildConfig mConfig;
    public MethodTracer(TraceBuildConfig config){
        this.mConfig=config;
    }
    public void trace(Map<File, File> dirMap,Map<File,File> jarMap){
        traceMethodFromSrc(dirMap);
        traceMethodFromJar(jarMap);
    }
    private void traceMethodFromSrc(Map<File, File> dirMap) {
        if (dirMap!=null){
            for (Map.Entry<File,File> entry:dirMap.entrySet()){
                innerTraceMethodFromSrc(entry.getKey(),entry.getValue());
            }
        }
    }

    private void traceMethodFromJar(Map<File, File> jarMap) {
        if (jarMap!=null){
           for (Map.Entry<File,File> entry:jarMap.entrySet()){
               innerTraceMethodFromJar(entry.getKey(),entry.getValue());
           }
        }
    }
    private void innerTraceMethodFromJar(File input, File output) {
        ZipOutputStream zipOutputStream=null;
        ZipFile zipFile=null;

        try {
            zipOutputStream=new ZipOutputStream(new FileOutputStream(output));
            zipFile=new ZipFile(input);
            Enumeration<? extends ZipEntry> enumeration=zipFile.entries();
            while (enumeration.hasMoreElements()){
                ZipEntry zipEntry=enumeration.nextElement();
                String zipEntryName=zipEntry.getName();
                if (mConfig.isNeedTraceClass(zipEntryName)){
                    InputStream inputStream=zipFile.getInputStream(zipEntry);
                    ClassReader classReader=new ClassReader(inputStream);
                    ClassWriter classWriter=new ClassWriter(ClassWriter.COMPUTE_MAXS);
                    ClassVisitor classVisitor=new TraceClassAdapter(Opcodes.ASM5,classWriter);
                    classReader.accept(classVisitor,ClassReader.EXPAND_FRAMES);

                    byte[] data=classWriter.toByteArray();
                    InputStream byteArrayInputStream=new ByteArrayInputStream(data);
                    ZipEntry newZipEntry=new ZipEntry(zipEntryName);
                    Util.addZipEntry(zipOutputStream,newZipEntry,byteArrayInputStream);

                }else {
                    InputStream inputStream=zipFile.getInputStream(zipEntry);
                    ZipEntry newZipEntry=new ZipEntry(zipEntryName);
                    Util.addZipEntry(zipOutputStream,newZipEntry,inputStream);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (zipOutputStream!=null){
                    zipOutputStream.finish();
                    zipOutputStream.flush();
                    zipOutputStream.close();
                }
                if (zipFile!=null){
                    zipFile.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }


    }

    private void innerTraceMethodFromSrc(File input, File output) {
        List<File> classFileList=new ArrayList<>();
        if (input.isDirectory()){
            listClassFiles(classFileList,input);
        }else {
            classFileList.add(input);
        }
        for (File classFile:classFileList){
            InputStream is=null;
            FileOutputStream os=null;
            try {
                final String changedFileInputFullPath=classFile.getAbsolutePath();
                final File changedFileOutput=new File(changedFileInputFullPath.replace(input.getAbsolutePath(),output.getAbsolutePath()));
                if (!changedFileOutput.exists()){
                    changedFileOutput.getParentFile().mkdirs();
                }
                changedFileOutput.createNewFile();

                if (mConfig.isNeedTraceClass(classFile.getName())){
                    is=new FileInputStream(classFile);
                    ClassReader classReader=new ClassReader(is);
                    ClassWriter classWriter=new ClassWriter(ClassWriter.COMPUTE_MAXS);
                    ClassVisitor classVisitor=new TraceClassAdapter(Opcodes.ASM5,classWriter);
                    classReader.accept(classVisitor,ClassReader.EXPAND_FRAMES);
                    is.close();

                    if (output.isDirectory()){
                        os=new FileOutputStream(changedFileOutput);
                    }else {
                        os=new FileOutputStream(output);
                    }
                    os.write(classWriter.toByteArray());
                    os.close();

                }else {
                    FileUtils.copyFile(classFile,changedFileOutput);
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                Util.closeQuietly(is);
                Util.closeQuietly(os);
            }



        }
    }
    private void listClassFiles(List<File> classFiles,File dir){
        File[] files=dir.listFiles();
        if (files==null){
            return;
        }
        for (File file:files){
            if (file==null){
                continue;
            }
            if (file.isDirectory()){
                listClassFiles(classFiles,file);
            }else {
                if (file!=null && file.isFile()){
                    classFiles.add(file);
                }
            }
        }
    }





    private class TraceClassAdapter extends ClassVisitor{
        private boolean mAbsClass=false;
        private String className;
        public TraceClassAdapter(int api, ClassVisitor classVisitor) {
            super(api, classVisitor);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
            mAbsClass=(access & Opcodes.ACC_ABSTRACT)>0 || (access&Opcodes.ACC_INTERFACE)>0;
            className=name;

            //System.out.println("TraceClassAdapter->visit"+className);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            if (mAbsClass) {
                return super.visitMethod(access, name, descriptor, signature, exceptions);
            }else {
                MethodVisitor methodVisitor=cv.visitMethod(access,name,descriptor,signature,exceptions);
                return new TraceMethodAdapter(api,methodVisitor,access,name,descriptor,className);
            }
        }
    }

    private class TraceMethodAdapter extends AdviceAdapter{
        private String mClassName;

        protected TraceMethodAdapter(int api, MethodVisitor methodVisitor, int access,
                                     String name, String descriptor,String className) {
            super(api, methodVisitor, access, name, descriptor);
            mClassName=className;
            TraceMethod traceMethod=TraceMethod.create(0,access,className,name,descriptor);
           // System.out.println(traceMethod.getMethodName());

        }

        @Override
        protected void onMethodEnter() {
            super.onMethodEnter();
            if (TraceBuildConfig.TRACE_CLASS.equals(mClassName)){
                return;
            }
            String sectionName=getName();
           // System.out.println("TraceMethodAdapter->onMethodEnter"+sectionName);
            mv.visitLdcInsn(sectionName);
            mv.visitMethodInsn(INVOKESTATIC,TraceBuildConfig.TRACE_CLASS,"i","(Ljava/lang/String;)V",false);
        }

        @Override
        protected void onMethodExit(int opcode) {
            super.onMethodExit(opcode);
            if (TraceBuildConfig.TRACE_CLASS.equals(mClassName)){
                return;
            }
            String sectionName=getName();
            mv.visitLdcInsn(sectionName);
            mv.visitMethodInsn(INVOKESTATIC, TraceBuildConfig.TRACE_CLASS, "o", "(Ljava/lang/String;)V", false);
        }
    }
}

