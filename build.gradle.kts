// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {

        allprojects{
            mavenCentral()
            maven ( url = "https://jitpack.io" )
            jcenter()
        }

        // other repositories...

    }
}

plugins {
    id("com.android.application") version "8.2.1" apply false
    id ("com.android.library") version "7.1.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    id("org.jetbrains.kotlin.jvm") version "1.7.0" apply false
    id("com.google.gms.google-services") version "4.3.10" apply false
}

