package com.wind.gradle.plugin.autotrackclick

import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.regex.Matcher


class AnalyticsClassInspector {


    private static String path2ClassName(String path) {
        path.replace(File.separator, ".").replace(".class", "")
    }

    private static byte[] modifyClass(byte[] srcBytes) {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS)
        ClassVisitor classVisitor = new AnalyticsClassVisitor(classWriter)

        ClassReader classReader = new ClassReader(srcBytes)
        classReader.accept(classVisitor, ClassReader.SKIP_FRAMES)

        return classWriter.toByteArray()

    }

    static File inspectClassFile(File dir, File classFile, File tempDir) {


        File modified = null
        try {


            String className = path2ClassName(classFile.absolutePath.replace(dir.absolutePath + File.separator, ""))
            byte[] sourceClassBytes = IOUtils.toByteArray(new FileInputStream(classFile))
            byte[] modifiedClassBytes = modifyClass(sourceClassBytes)
            if (modifiedClassBytes) {
                modified = new File(tempDir, className.replace('.', '/') + '.class')
                //println(modified)
                if (modified.exists()) {
                    modified.delete()
                }
                if (!modified.getParentFile().exists()){
                    modified.getParentFile().mkdirs()
                }
                modified.createNewFile()
                new FileOutputStream(modified).write(modifiedClassBytes)
            }
        } catch (Exception e) {
            e.printStackTrace()
            modified = classFile
        }
        return modified
    }

    static File inspectJar(File jarFile,File tempDir,String  md5HexName){
        //src jarFile
        def srcJarFile=new JarFile(jarFile,false)


        if (md5HexName==null){
            md5HexName=""
        }
        //dest
        def dest=new File(tempDir,md5HexName+jarFile.name)

        JarOutputStream jarOutputStream=new JarOutputStream(new FileOutputStream(dest))

        Enumeration<JarEntry> enumeration= srcJarFile.entries()
        while (enumeration.hasMoreElements()){

            JarEntry jarEntry= enumeration.nextElement()
            InputStream inputStream=srcJarFile.getInputStream(jarEntry)
            String entryName=jarEntry.getName()
            boolean dsaOrSF=entryName.endsWith(".DSA") || entryName.endsWith(".SF")
            if (!dsaOrSF){
                String className
                JarEntry outputJarEntry=new JarEntry(entryName)
                jarOutputStream.putNextEntry(outputJarEntry)
                byte[] modifiedClassBytes=null
                byte[] sourceClassBytes=IOUtils.toByteArray(inputStream)
                if (entryName.endsWith(".class")){
                    className = entryName.replace(Matcher.quoteReplacement(File.separator), ".").replace(".class", "")
                    modifiedClassBytes=  modifyClass(sourceClassBytes)
                }

                if (modifiedClassBytes==null){
                    modifiedClassBytes=sourceClassBytes
                }
                jarOutputStream.write(modifiedClassBytes)
                jarOutputStream.closeEntry()
            }

        }

        jarOutputStream.close()
        srcJarFile.close()
        return dest
    }
}