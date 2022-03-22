package com.wind.gradle.plugin.systrace

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Status
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import com.google.common.hash.Hashing
import com.wind.gradle.plugn.MethodTracer
import com.wind.gradle.plugn.TraceBuildConfig
import com.wind.gradle.plugn.Util
import org.apache.commons.compress.utils.Charsets

import java.lang.reflect.Field
import java.nio.charset.Charset

class SystraceTransform extends Transform {
    private SystraceExtension mExtension
    SystraceTransform(SystraceExtension extension){
        mExtension=extension
    }
    @Override
    String getName() {
        return "SystraceTransform"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return true
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {

        final boolean isIncremental = transformInvocation.isIncremental() && this.isIncremental()
        TransformOutputProvider outputProvider=transformInvocation.outputProvider
        //File temporaryDir = transformInvocation.context.temporaryDir
        //final File rootOutput = temporaryDir

        //保存修改过的输入jar 输出jar文件
        Map<File, File> jarInputMap = new HashMap<>()
        //保存修改过的输入文件 输出文件
        Map<File, File> srcInputMap = new HashMap<>()


        transformInvocation.inputs.each { input ->
            input.directoryInputs.each { dirInput ->
                //dirInput 对应的输出位置
                def outputDir = outputProvider.getContentLocation(dirInput.name,
                        dirInput.contentTypes, dirInput.scopes, Format.DIRECTORY)
                System.out.println("SystraceTransform outputDir:${outputDir.getAbsolutePath()}");
                collectAndIdentifyDir(srcInputMap, dirInput, outputDir, isIncremental)


            }
            input.jarInputs.each { jarInput ->
                if (jarInput.getStatus() != Status.REMOVED){
                    //获得输出路径
                    def outputJarLocation=outputProvider.getContentLocation(getUniqueJarName(jarInput.file),
                            jarInput.contentTypes, jarInput.scopes, Format.JAR)
                    System.out.println("SystraceTransform  outputJarLocation:${outputJarLocation.getAbsolutePath()}");
                    collectAndIdentifyJar(jarInputMap, jarInput, outputJarLocation, isIncremental)
                }

            }
        }
        TraceBuildConfig config=new TraceBuildConfig()
        MethodTracer methodTracer=new MethodTracer(config)
        methodTracer.trace(srcInputMap,jarInputMap)
    }

    private void collectAndIdentifyJar(Map<File, File> jarInputMap, JarInput input, File jarOutput, boolean isIncremental) {
        final File jarInput = input.file
        //final File jarOutput = new File(rootOutput, getUniqueJarName(jarInput))
        if (Util.isRealZipOrJar(jarInput)) {
            switch (input.status) {
                case Status.NOTCHANGED:
                    if (isIncremental) {
                        break
                    }
                case Status.ADDED:
                case Status.CHANGED:
                    jarInputMap.put(jarInput, jarOutput)
                    break
                case Status.REMOVED:
                    break
            }

        }
        //replaceFile(input,jarOutput)
    }


    private void collectAndIdentifyDir(Map<File, File> dirInputMap, DirectoryInput input, File dirOutput, boolean isIncremental) {
        final File dirInput = input.file
       // final File dirOutput = new File(rootOutput, input.file.getName())
        if (!dirOutput.exists()) {
            dirOutput.mkdirs()
        }
        if (isIncremental) {
            if (!dirInput.exists()) {
                dirOutput.deleteDir()
            } else {
                //保存修改过的文件
                final Map<File, Status> obfuscatedChangedFiles = new HashMap<>()
                final String rootInputFullPath = dirInput.getAbsolutePath()
                final String rootOutputFullPath = dirOutput.getAbsolutePath()
                input.changedFiles.each { entry ->
                    //输入文件
                    final File changedFileInput = entry.getKey()
                    final String changedFileInputFullPath = changedFileInput.getAbsolutePath()
                    //对应的输出文件
                    final File changedFileOutput = new File(
                            changedFileInputFullPath.replace(rootInputFullPath, rootOutputFullPath)
                    )
                    final Status status = entry.getValue()
                    switch (status) {
                        case Status.NOTCHANGED:
                            break
                        case Status.ADDED:
                        case Status.CHANGED:
                            dirInputMap.put(changedFileInput, changedFileOutput)
                            break
                        case Status.REMOVED:
                            changedFileOutput.delete()
                            break
                    }
                    obfuscatedChangedFiles.put(changedFileOutput, status)
                }
                //replaceChangedFile(input, obfuscatedChangedFiles)
            }

        } else {
            dirInputMap.put(dirInput, dirOutput)
        }
      //  replaceFile(input, dirOutput)
    }

    protected void replaceFile(QualifiedContent input, File newFile) {
        final Field fileField = ReflectUtil.getDeclaredFieldRecursive(input.getClass(), 'file')
        fileField.set(input, newFile)
    }

    protected void replaceChangedFile(DirectoryInput dirInput, Map<File, Status> changedFiles) {
        final Field changedFilesField = ReflectUtil.getDeclaredFieldRecursive(dirInput.getClass(), 'changedFiles')
        changedFilesField.set(dirInput, changedFiles)
    }

    protected String getUniqueJarName(File jarFile) {
        final String originJarName = jarFile.getName()
        final String hashing = Hashing.sha1().hashString(jarFile.getPath(), Charsets.UTF_16LE).toString()
        final int dotPos = originJarName.lastIndexOf('.')
        if (dotPos < 0) {
            return "${originJarName}_${hashing}"
        } else {
            final String nameWithoutDotExt = originJarName.substring(0, dotPos)
            final String dotExt = originJarName.substring(dotPos)
            return "${nameWithoutDotExt}_${hashing}${dotExt}"
        }

    }


}