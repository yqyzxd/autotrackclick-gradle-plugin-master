package com.wind.analytics

import android.view.View
import androidx.annotation.Keep
import org.json.JSONObject

/**
 * created by wind on 1/14/21:3:10 PM
 */
object AutoAnalyticsHelper {



    @Keep
    @JvmStatic
    fun trackViewOnClick(view: View){

        var jsonObject=JSONObject()
        jsonObject.put("\$element_type",AnalyticsImpl.getViewType(view))
        jsonObject.put("\$element_id",AnalyticsImpl.getViewId(view))
        jsonObject.put("\$element_content",AnalyticsImpl.getViewText(view))

        var activity = AnalyticsImpl.getActivityFromView(view)
        if (activity!=null){
            jsonObject.put("\$activity",activity::class.java.canonicalName)
        }

        Analytics.track("\$AppClick",jsonObject)
    }

}