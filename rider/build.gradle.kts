plugins {
        alias(libs.plugins.convention.android.application)
}

android {
        namespace = "org.dmn.template.rider"

        defaultConfig {
                applicationId = "org.dmn.template.rider"
                val buildNumber = (project.findProperty("appBuildNumber") as String?)
                        ?.toIntOrNull() ?: 1
                versionCode = buildNumber
                versionName = "0.1.$buildNumber"
        }
}

dependencies {
        implementation(project(":core"))
}