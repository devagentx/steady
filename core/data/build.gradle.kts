plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "ai.monkmind.steady.core.data"
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
    implementation(projects.core.database)
    implementation(projects.core.domain)
    implementation(projects.core.model)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.hilt.android)
    implementation(libs.kotlinx.coroutines.core)
    ksp(libs.hilt.compiler)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
