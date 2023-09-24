@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    kotlin("kapt")
    alias(libs.plugins.hilt)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.apolloGraphql)
    alias(libs.plugins.ksp)
}

android {
    namespace = "studio1a23.lyricovaJukebox"
    compileSdk = 34

    defaultConfig {
        applicationId = "studio1a23.lyricovaJukebox"
        minSdk = 31
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    apollo {
        service("service") {
            packageName.set("studio1a23.lyricovaJukebox")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.material.icons)
    implementation(libs.material.colors)
    implementation(libs.composeRuntime)
    implementation(libs.composeRuntime.liveData)
    implementation(libs.navigation.compose)
    implementation(libs.navigation.runtime)
    implementation(libs.datastore.preferences)
    implementation(libs.jwt)
    implementation(libs.gson)
    implementation(libs.apolloRuntime)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.inject)
    implementation(libs.hilt.android)
    implementation(libs.hilt.gradle)
    implementation(libs.workRuntime)
    implementation(libs.androidx.hilt.common)
    implementation(libs.androidx.hilt.navigation)
    implementation(libs.androidx.hilt.work)
    implementation(libs.javapoet)
    implementation(libs.kotlinpoet.javapoet)
    implementation(libs.kotlinpoet)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)
    implementation(libs.media3.common)
    implementation(libs.media3.session)
    implementation(libs.media3.exoplayerDash)
    implementation(libs.guava)
    implementation(libs.concurrentFutures)
    implementation(libs.kotlinxCoroutinesGuava)
    implementation(libs.volley)
    implementation(libs.palette)
    implementation(libs.coil)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
    kapt(libs.hilt.compiler)
    kapt(libs.hilt.androidCompiler)
    kapt(libs.androidx.hilt.compiler)
    ksp(libs.room.compiler)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}

//configurations.all {
//    resolutionStrategy.eachDependency {
//        when (requested.name) {
//            "javapoet" -> useVersion("1.13.0")
//        }
//    }
//}
