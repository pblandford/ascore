plugins {
    kotlin("jvm")
}


dependencies {

    implementation(libs.commons.math3)
    implementation(libs.commons.io)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlin.stdlib)
    testImplementation(libs.junit)

  //  testImplementation group: 'junit', name: 'junit', version: '4.12'
}

