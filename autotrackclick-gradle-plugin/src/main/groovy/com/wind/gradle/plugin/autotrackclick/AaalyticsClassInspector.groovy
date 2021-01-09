package com.wind.gradle.plugin.autotrackclick

import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter


class AaalyticsClassInspector {


    static String path2ClassName(String path) {
        path.replace(File.separator, ".").replace(".class", "")

    }

    static byte[] modifyClass(byte[] srcBytes) {
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
}