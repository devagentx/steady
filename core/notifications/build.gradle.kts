plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "ai.monkmind.steady.core.notifications"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(projects.core.model)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.hilt.android)
    implementation(libs.kotlinx.coroutines.android)
    ksp(libs.androidx.hilt.compiler)
    ksp(libs.hilt.compiler)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
