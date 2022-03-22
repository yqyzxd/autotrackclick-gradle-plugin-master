package com.wind.gradle.plugn;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import groovyjarjarpicocli.CommandLine;

public class Util {
    public static final int BUFFER_SIZE = 16384;

    public static void addZipEntry(ZipOutputStream zipOutputStream,
                                   ZipEntry zipEntry,
                                   InputStream inputStream) throws Exception {
        try {
            zipOutputStream.putNextEntry(zipEntry);
            byte[] buffer = new byte[BUFFER_SIZE];
            int len = -1;
            while ((len=inputStream.read(buffer, 0, buffer.length)) != -1) {
                zipOutputStream.write(buffer, 0, len);
                zipOutputStream.flush();
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            closeQuietly(inputStream);
            zipOutputStream.closeEntry();
        }

    }


    public static boolean isRealZipOrJar(File input) {
        ZipFile zf = null;

        try {
            zf = new ZipFile(input);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Util.closeQuietly(zf);
        }
        return false;
    }


    public static void closeQuietly(AutoCloseable target) {
        if (target == null) {
            return;
        }
        try {
            target.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
