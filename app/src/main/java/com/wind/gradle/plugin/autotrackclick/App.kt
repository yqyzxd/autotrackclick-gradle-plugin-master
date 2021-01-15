package com.wind.gradle.plugin.autotrackclick

import android.app.Application
import com.wind.analytics.Analytics

/**
 * created by wind on 1/14/21:4:34 PM
 */
class App : Application() {


    override fun onCreate() {
        super.onCreate()

        Analytics.init(this)
    }

}