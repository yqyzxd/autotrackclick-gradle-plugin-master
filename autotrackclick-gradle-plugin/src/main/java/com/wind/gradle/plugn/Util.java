package com.wind.gradle.plugn;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

import groovyjarjarpicocli.CommandLine;

public class Util {


    public static boolean isRealZipOrJar(File input){
        ZipFile zf=null;

        try {
            zf = new ZipFile(input);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            Util.closeQuietly(zf);
        }
        return false;
    }


    public static void closeQuietly(AutoCloseable target){
        if (target==null){
            return;
        }
        try {
            target.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
