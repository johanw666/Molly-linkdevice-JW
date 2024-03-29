// MOLLY: Edit Dockerfile to download the same SDK packages
val signalBuildToolsVersion by extra("32.0.0")
val signalCompileSdkVersion by extra("android-33")
val signalTargetSdkVersion by extra(31)
// JW: changed minSDK from 24 to 23
val signalMinSdkVersion by extra(23)
val signalJavaVersion by extra(JavaVersion.VERSION_11)
val signalKotlinJvmTarget by extra("11")