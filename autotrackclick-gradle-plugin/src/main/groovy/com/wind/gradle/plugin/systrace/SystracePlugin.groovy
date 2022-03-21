
package com.wind.gradle.plugin.systrace

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project


class SystracePlugin implements Plugin<Project>{

    @Override
    void apply(Project project) {

        SystraceExtension extension=project.extensions.create("systrace",SystraceExtension)
        AppExtension android=project.extensions.findByType(AppExtension)
        android.registerTransform(new SystraceTransform(extension))
    }
}
