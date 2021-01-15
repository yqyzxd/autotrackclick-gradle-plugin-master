package com.wind.analytics

import android.app.Application
import android.util.Log
import androidx.annotation.NonNull
import org.json.JSONObject

/**
 * created by wind on 1/12/21:3:27 PM
 */
object Analytics {
    @JvmStatic final var TAG:String = "Analytics"

    @JvmStatic final var SDK_VERSION:String = "1.0.0"


    var mInited = false
    lateinit var mDeviceId:String

    var mDeviceInfo:Map<String,Any>?=null

    @JvmStatic
    fun init(application: Application) {
        if (!mInited) {
            mInited = true
            //todo 获取设备信息以及oaid
            mDeviceId = AnalyticsImpl.getAndroidID(application.applicationContext)
            mDeviceInfo = AnalyticsImpl.getDeviceInfo(application.applicationContext)
        }
    }


    fun track(@NonNull eventName:String,properties:JSONObject){
        if (!mInited){
            Log.e(TAG,"have do call init(application) ?")
            return
        }
        var jsonObject=JSONObject()
        jsonObject.put("event",eventName)
        jsonObject.put("device_id",mDeviceId)

        var deviceJSONObject=JSONObject(mDeviceInfo)
        if (properties!=null){
            AnalyticsImpl.mergeJSONObject(properties,deviceJSONObject)
        }

        jsonObject.put("properties",deviceJSONObject)
        jsonObject.put("time",System.currentTimeMillis())


        Log.i(TAG,AnalyticsImpl.formatJson(jsonObject.toString()))

    }




}