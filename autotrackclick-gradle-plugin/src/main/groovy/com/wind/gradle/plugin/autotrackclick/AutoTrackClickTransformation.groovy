package com.wind.gradle.plugin.autotrackclick

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Context
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import groovy.io.FileType
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils

class AutoTrackClickTransformation extends Transform {

    @Override
    String getName() {
        return "AutoTrackClick"
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
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        Collection<TransformInput> inputs = transformInvocation.getInputs()
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider()

        if (!incremental) {
            //不支持增量编译，删除所有产出物
            outputProvider.deleteAll()
        }
        //input有两种 一种是目录(本地源码)，一种是jar包
        inputs.each {
            TransformInput input ->
                //处理目录（自身源码）
                doDirectoryInputs(transformInvocation.context,input.directoryInputs, outputProvider)
                //处理jar包 包括aar
                doJarInputs(input.jarInputs, outputProvider)
        }

    }

    void doJarInputs(Context context,Collection<JarInput> jarInputs, TransformOutputProvider outputProvider) {
        jarInputs.each {
            JarInput jarInput ->
                //需要重新命名输出文件 同一个目录拷贝会冲突
                def jarName = jarInput.name
                def md5Name = DigestUtils.md5Hex(jarInput.file.absolutePath).substring(0,8)
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }



                //获得输出路径
                def dest=outputProvider.getContentLocation(jarName + md5Name,
                        jarInput.contentTypes, jarInput.scopes, Format.JAR)

                def modifiedJar=null
                modifiedJar= AaalyticsClassInspector.inspectJar(jarInput.file,context.getTemporaryDir(),md5Name)
                if (modifiedJar==null){
                    modifiedJar = jarInput.file
                }
                FileUtils.copyFile(copyJarFile,dest)
        }
    }

    void doDirectoryInputs(Context context,Collection<DirectoryInput> directoryInputs, TransformOutputProvider outputProvider) {
        directoryInputs.each {
            DirectoryInput directoryInput ->
                //目标输出目录
                def dest = outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                File dir=directoryInput.file
                if (dir){

                    Map<String,File> modifiedMap=new HashMap<>()
                    //遍历以.class结尾的文件
                    dir.traverse(type: FileType.FILES,nameFilter:~/.*\.class/) {
                        File classFile->
                            File modified = null
                            modified=AaalyticsClassInspector.inspectClassFile(dir,classFile,context.getTemporaryDir())
                            println(getName()+modified.absolutePath)
                            if (modified!=null){
                                String key= classFile.absolutePath.replace(dir.absolutePath,"")
                                println(getName()+key)
                                modifiedMap.put(key,modified)
                            }
                    }

                    //将input目录 复制到dest目录
                    FileUtils.copyDirectory(directoryInput.file, dest)

                    //将修改的文件拷贝到目标文件
                    modifiedMap.entrySet().each{
                        Map.Entry<String,File> entry->
                            File target=new File((dest.absolutePath+ entry.getKey()))
                            if (target.exists()){
                                target.delete()
                            }
                            println("target:"+target.absolutePath)
                            FileUtils.copyFile(entry.getValue(),target)
                            entry.getValue().delete()

                    }

                }
        }
    }
}