plugins {
        alias(libs.plugins.convention.android.application)
}

android {
        namespace = "org.dmn.template.admin"

        defaultConfig {
                applicationId = "org.dmn.template.admin"
                val buildNumber = (project.findProperty("appBuildNumber") as String?)
                        ?.toIntOrNull() ?: 1
                versionCode = buildNumber
                versionName = "0.1.$buildNumber"
        }
}

dependencies {
        implementation(project(":core"))
}