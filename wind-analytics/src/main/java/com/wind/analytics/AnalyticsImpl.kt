package com.wind.analytics

import android.annotation.TargetApi
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.widget.RatingBar
import android.widget.SeekBar
import android.widget.TextView
import android.widget.ToggleButton
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/**
 * created by wind on 1/12/21:3:36 PM
 */
internal class AnalyticsImpl {

    companion object {
        private var mDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CHINA)
        private var mIgnoredActivities: MutableList<Int> = mutableListOf()

        @JvmStatic fun ignoreAutoTrackActivity(activity: Class<*>) {
            mIgnoredActivities.add(activity.hashCode())
        }

        @JvmStatic fun removeIgnoredActivity(activity: Class<*>) {
            if (mIgnoredActivities.contains(activity.hashCode())) {
                mIgnoredActivities.remove(activity.hashCode())
            }
        }

        @JvmStatic fun mergeJSONObject(src: JSONObject, dest: JSONObject) {
            src.keys().forEach { key: String ->
                var value = src[key]
                if (value is Date) {
                    dest.put(key, mDateFormat.format(value))
                } else {
                    dest.put(key, value)
                }
            }
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @JvmStatic private fun getToolbarTitle(activity: Activity): String? {
            var actionBar = activity.actionBar
            if (actionBar != null) {

                var title = actionBar.title
                if (!TextUtils.isEmpty(title)) {
                    return title.toString()
                }
            } else {

                if (activity is AppCompatActivity) {
                    var supportActionBar = activity.supportActionBar
                    if (supportActionBar != null) {
                        var title = supportActionBar.title
                        if (!TextUtils.isEmpty(title)) {
                            return title.toString()
                        }
                    }

                }

            }
            return null
        }


        @JvmStatic private fun getActivityTitle(activity: Activity): String? {

            var activityTitle: String? = null
            if (activity != null) {
                if (!TextUtils.isEmpty(activity.title)) {
                    activityTitle = activity.title.toString()
                }
                if (Build.VERSION.SDK_INT >= 11) {
                    var toolbarTitle = getToolbarTitle(activity)
                    if (!TextUtils.isEmpty(toolbarTitle)) {
                        activityTitle = toolbarTitle
                    }
                }

                if (TextUtils.isEmpty(activityTitle)) {

                    var packageManager = activity.packageManager
                    if (packageManager != null) {
                        var activityInfo = packageManager.getActivityInfo(activity.componentName, 0)
                        if (activity != null) {
                            var label = activityInfo.loadLabel(packageManager)
                            if (!TextUtils.isEmpty(label)) {
                                activityTitle = label.toString()
                            }
                        }
                    }

                }
            }
            return activityTitle
        }

        /**
         * Track 页面浏览事件 需要反射调用该方法因此需要添加keep
         */
        @Keep
        @JvmStatic private fun trackAppViewScreen(activity: Activity){

            if (activity!=null){


                if (mIgnoredActivities.contains(activity::class.java.hashCode())){

                    var properties=JSONObject()
                    properties.put("${'$'}activity",activity::class.java.canonicalName)
                    properties.put("title", getActivityTitle(activity))
                    Analytics.track("${'$'}AppViewScreen",properties)
                }

            }
        }


        fun registerActivityLifecycleCallbacks(application:Application){
            application.registerActivityLifecycleCallbacks(object :Application.ActivityLifecycleCallbacks{
                override fun onActivityPaused(activity: Activity) {
                }

                override fun onActivityStarted(activity: Activity) {
                }

                override fun onActivityDestroyed(activity: Activity) {
                }

                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                }

                override fun onActivityStopped(activity: Activity) {
                }

                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                }

                override fun onActivityResumed(activity: Activity) {
                    trackAppViewScreen(activity)
                }

            })
        }


        fun getDeviceInfo(context:Context):Map<String,Any>{

            val deviceInfo= mutableMapOf<String,Any>()
            deviceInfo.put("${'$'}lib","Android")
            deviceInfo.put("${'$'}lib_version",Analytics.SDK_VERSION)
            deviceInfo.put("${'$'}os","Android")
            var os_version=Build.VERSION.RELEASE ?:"UNKNOWN"
            deviceInfo.put("${'$'}os_version",os_version)

            var manufacturer=Build.MANUFACTURER ?:"UNKNOWN"
            deviceInfo.put("${'$'}manufacturer",manufacturer)
            var model=Build.MODEL?:"UNKNOWN"
            deviceInfo.put("${'$'}model",model.trim())


           val packageInfo= context.packageManager.getPackageInfo(context.packageName,0)

            deviceInfo.put("${'$'}app_version",packageInfo.versionName)

            var labelRes=packageInfo.applicationInfo.labelRes

            deviceInfo.put("${'$'}app_name", context.resources.getString(labelRes))


            var displayMetrics=context.resources.displayMetrics
            deviceInfo.put("${'$'}screen_height",displayMetrics.heightPixels)
            deviceInfo.put("${'$'}screen_width",displayMetrics.widthPixels)



            return deviceInfo
        }

        fun getAndroidID(context: Context):String{
            var androidId=Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            return androidId
        }


        /**
         * 获取view的android:id对应的字符串
         */
        fun  getViewId(view: View):String{
            var idStr=""
            if (view.id != View.NO_ID){
                 idStr=view.context.resources.getResourceEntryName(view.id)
            }
            return idStr

        }

        fun getViewType(view: View):String{

            if (view==null){
                return ""
            }
            return view.javaClass.simpleName

        }

        fun getViewText(view: View):String{
            var text:CharSequence?=null
            when(view){
                is ToggleButton -> {
                    if (view.isChecked){
                        text=view.textOn
                    }else{
                        text=view.textOff
                    }
                }
                is TextView ->{
                    if (TextUtils.isEmpty(text)){
                        text = view.text
                    }
                }
                is SeekBar -> text=view.progress.toString()

                is RatingBar -> text= view.rating.toString()
            }

            if (text!=null){
                return text.toString()
            }
            return ""
        }


        fun getActivityFromView(view:View):Activity?{
            if (view == null){
                return null
            }

            var context=view.context
            var activity= getActivityFromContext(context)

            return activity

        }

        fun getActivityFromContext(context: Context):Activity?{
            var activity:Activity?=null
            if (context!=null){
                if (context is Activity){
                    activity=context
                }else if (context is ContextWrapper){
                    var ctx=context
                    while ( ctx !is Activity && ctx is ContextWrapper ){
                        ctx= ctx.baseContext
                    }
                    if (ctx is Activity){
                        activity=ctx
                    }
                }
            }

            return activity
        }

        private fun addIndentBlank(sb: java.lang.StringBuilder, indent: Int) {
            try {
                for (i in 0 until indent) {
                    sb.append('\t')
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        fun formatJson(jsonStr: String?): String {
            return try {
                if (null == jsonStr || "" == jsonStr) {
                    return ""
                }
                val sb = StringBuilder()
                var last: Char
                var current = '\u0000'
                var indent = 0
                var isInQuotationMarks = false
                for (i in 0 until jsonStr.length) {
                    last = current
                    current = jsonStr[i]
                    when (current) {
                        '"' -> {
                            if (last != '\\') {
                                isInQuotationMarks = !isInQuotationMarks
                            }
                            sb.append(current)
                        }
                        '{', '[' -> {
                            sb.append(current)
                            if (!isInQuotationMarks) {
                                sb.append('\n')
                                indent++
                                addIndentBlank(
                                    sb,
                                    indent
                                )
                            }
                        }
                        '}', ']' -> {
                            if (!isInQuotationMarks) {
                                sb.append('\n')
                                indent--
                                addIndentBlank(
                                    sb,
                                    indent
                                )
                            }
                            sb.append(current)
                        }
                        ',' -> {
                            sb.append(current)
                            if (last != '\\' && !isInQuotationMarks) {
                                sb.append('\n')
                                addIndentBlank(
                                    sb,
                                    indent
                                )
                            }
                        }
                        else -> sb.append(current)
                    }
                }
                sb.toString()
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
        }

    }



}