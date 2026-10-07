plugins {
        alias(libs.plugins.convention.android.application)
}

android {
        namespace = "org.dmn.template"

        defaultConfig {
                applicationId = "org.dmn.template"
                // GitHub Actions passes its run number, so every build has a higher
                // version code and phones always accept it as an update.
                val buildNumber = (project.findProperty("appBuildNumber") as String?)
                        ?.toIntOrNull() ?: 1
                versionCode = buildNumber
                versionName = "0.1.$buildNumber"
        }
}

dependencies {
        implementation(project(":core"))
        implementation(project(":rider"))
}