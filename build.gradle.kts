plugins {
    application
    id("com.gradleup.shadow") version "8.3.1"
}

application.mainClass = "com.yukiDev.Yuki.Bot" //


group = "org.yukiDev"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.dv8tion:JDA:5.3.2")
    implementation("ch.qos.logback:logback-classic:1.5.18")
    implementation("org.mongodb:mongodb-driver-sync:5.3.0")
    implementation("com.google.code.gson:gson:2.12.0")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.isIncremental = true

    // Set this to the version of java you want to use,
    // the minimum required for JDA is 1.8
    sourceCompatibility = "1.8"
}