package com.wind.gradle.plugin.autotrackclick

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class AutoTrackClickPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        AppExtension android= project.extensions.findByType(AppExtension)
        android.registerTransform(new AutoTrackClickTransformation())
    }
}