plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "ai.monkmind.steady.core.database"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    api(projects.core.model)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.hilt.android)
    implementation(libs.kotlinx.coroutines.core)
    ksp(libs.androidx.room.compiler)
    ksp(libs.hilt.compiler)
    testImplementation(libs.junit)
}
