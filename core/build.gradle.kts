plugins {
        alias(libs.plugins.convention.android.library)
        alias(libs.plugins.kotlin.compose)
}

android {
        namespace = "org.dmn.template.core"
        buildFeatures {
                compose = true
        }
}

dependencies {
        implementation(platform(libs.findLibrary("androidx.compose.bom").get()))
        implementation(libs.findLibrary("androidx.compose.ui").get())
        implementation(libs.findLibrary("androidx.compose.ui.graphics").get())
        implementation(libs.findLibrary("androidx.compose.material3").get())
}