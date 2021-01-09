package com.wind.analytics;

/**
 * Created By wind
 * on 1/9/21
 */
public class ATime {

    static long start;
    public static void start(){
        start=System.currentTimeMillis();
    }

    public static void end(){
        long during=System.currentTimeMillis()-start;
        System.out.println("during:"+during);



    }
}
