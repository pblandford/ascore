plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    compileSdk = rootProject.extra["compileSdkVersion"] as Int

    defaultConfig {
        minSdk =  rootProject.extra["minSdkVersion"] as Int
        targetSdk = rootProject.extra["targetSdkVersion"] as Int

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
    }

    packagingOptions {
//        resources {
//            excludes += '/META-INF/{AL2.0,LGPL2.1}'
//        }
    }

    namespace = "org.philblandford.ascore2"
}


dependencies {
    implementation(libs.androidx.core)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.tooling.preview)
    implementation(libs.androidx.lifecycle)
    implementation(libs.androidx.activity.compose)
    implementation(libs.billing)
    implementation(libs.timber)
    implementation(libs.commons.math3)
    implementation(libs.commons.io)
    implementation(libs.gson)
    implementation(project(":kscore"))
    implementation(project(":kscoreandroid"))
    implementation(project(":mp3converter"))
    implementation(libs.kotlin.reflect)
    testImplementation(libs.junit)
}