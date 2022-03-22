package com.wind.gradle.plugin.autotrackclick;


import android.os.Trace;

public class TraceTag {

    public static void i(String sectionName){
        Trace.beginSection(sectionName);
        System.out.println("TraceTag i("+sectionName+")");
    }

    public static void o(String sectionName){
        System.out.println("TraceTag o("+sectionName+")");
        Trace.endSection();

    }
}
