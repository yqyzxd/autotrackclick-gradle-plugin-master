package com.wind.gradle.plugn;

import org.gradle.launcher.daemon.server.BadlyFormedRequestException;

import java.util.HashSet;

public class TraceBuildConfig {

    public static final String[] UN_TRACE_CLASS = {"R.class", "R$", "Manifest", "BuildConfig"};

    public static final String TRACE_CLASS="com/wind/gradle/plugin/autotrackclick/TraceTag";

    private  String mPackageName;
    private  String mMappingPath;
    private  String mBaseMethodMap;
    private  String mMethodMapFile;
    private  String mIgnoreMethodMapFile;

    private  String mBlackListDir;
    private  HashSet<String> mBlackClassMap;
    private  HashSet<String> mBlackPackageMap;

    public TraceBuildConfig(){

    }
    public TraceBuildConfig(String packageName, String mappingPath, String baseMethodMap, String methodMapFile, String ignoreMethodMapFile, String blackListFile) {
        mPackageName = packageName;
        mMappingPath = mappingPath;
        mBaseMethodMap = baseMethodMap;
        mMethodMapFile = methodMapFile;
        mIgnoreMethodMapFile = ignoreMethodMapFile;
        mBlackListDir = blackListFile;
        mBlackClassMap = new HashSet();
        mBlackPackageMap = new HashSet();
    }


    public boolean isNeedTraceClass(String fileName){
        boolean isNeed=true;
        if (fileName.endsWith(".class")){
            for (String unTraceCls:UN_TRACE_CLASS){
                if (fileName.contains(unTraceCls)){
                    isNeed=false;
                    break;
                }
            }
        }else {
            isNeed=false;
        }
        return isNeed;
    }


}
