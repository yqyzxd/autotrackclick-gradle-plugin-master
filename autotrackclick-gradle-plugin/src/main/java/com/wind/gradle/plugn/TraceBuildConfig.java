package com.wind.gradle.plugn;

import java.util.HashSet;

public class TraceBuildConfig {

    private final String mPackageName;
    private final String mMappingPath;
    private final String mBaseMethodMap;
    private final String mMethodMapFile;
    private final String mIgnoreMethodMapFile;

    private final String mBlackListDir;
    private final HashSet<String> mBlackClassMap;
    private final HashSet<String> mBlackPackageMap;

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



}
