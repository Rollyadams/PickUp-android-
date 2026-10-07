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
        implementation(platform(libs.androidx.compose.bom))
        implementation(libs.androidx.compose.ui)
        implementation(libs.androidx.compose.ui.graphics)
        implementation(libs.androidx.compose.material3)
}