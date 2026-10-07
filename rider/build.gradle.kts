plugins {
        alias(libs.plugins.convention.android.library)
        alias(libs.plugins.kotlin.compose)
}

android {
        namespace = "org.dmn.template.rider"
        buildFeatures {
                compose = true
        }
}

dependencies {
        implementation(project(":core"))
        implementation(platform(libs.findLibrary("androidx.compose.bom").get()))
        implementation(libs.findLibrary("androidx.compose.ui").get())
        implementation(libs.findLibrary("androidx.compose.ui.graphics").get())
        implementation(libs.findLibrary("androidx.compose.material3").get())
        implementation(libs.findLibrary("androidx.activity.compose").get())
        implementation(libs.findLibrary("androidx.navigation.compose").get())
        implementation(libs.findLibrary("androidx.core.ktx").get())
        implementation(libs.findLibrary("kotlinx.coroutines.android").get())
}