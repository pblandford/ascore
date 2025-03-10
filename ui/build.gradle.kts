plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.google.services)
    alias(libs.plugins.google.crashlytics)
    alias(libs.plugins.compose.compiler)
}

android {
    compileSdk = rootProject.extra["compileSdkVersion"] as Int
    namespace = "org.philblandford.ui"

    defaultConfig {
        applicationId = "com.philblandford.ascore"
        minSdk = rootProject.extra["minSdkVersion"] as Int
        targetSdk = rootProject.extra["targetSdkVersion"] as Int
        versionCode = 141
        versionName = "1.1.3c Platinum"

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
        buildConfig = true
    }

    buildTypes {
        debug {  }
        release {  }
    }

//    packagingOptions {
//        resources {
//            excludes += '/META-INF/{AL2.0,LGPL2.1}'
//        }
//    }
}

dependencies {

    api(project(":app"))
    api(project(":kscore"))
    api(project(":kscoreandroid"))


    // Koin
    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.compose)

    /* Logging */
    implementation(libs.timber)

    /* Maths */
    implementation(libs.commons.math3)
    implementation(libs.androidx.core)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.material)
    implementation(libs.compose.tooling.preview)
    implementation(libs.compose.material3.windowSizeClass)
    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.accompanist.webview)
    implementation(libs.accompanist.pager)

    implementation(libs.review.ktx)
    implementation(libs.compose.router)

    implementation(libs.commons.io)

    implementation(libs.compose.color.picker)
    implementation(libs.billing)
    implementation(libs.androidx.lifecycle)
    implementation(libs.androidx.activity.compose)

    testImplementation(libs.junit)
    testImplementation(project(":kscore"))



    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.storage)

}
