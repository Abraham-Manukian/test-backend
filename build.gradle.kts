import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    application
    kotlin("jvm") version "1.8.0"
    id("com.github.johnrengelman.shadow") version "5.0.0"
}

group = "mobi.sevenwinds"
version = "0.0.1-SNAPSHOT"

application {
    mainClass.set("mobi.sevenwinds.ApplicationKt")
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")

    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-jackson:$ktor_version")
    implementation("io.ktor:ktor-server-auth:$ktor_version") // Вместо ktor-auth
    implementation("io.ktor:ktor-server-auth-jwt:$ktor_version") // Вместо ktor-auth-jwt
    implementation("io.ktor:ktor-server-metrics-micrometer:$ktor_version") // Вместо ktor-metrics


    implementation("ch.qos.logback:logback-classic:$logback_version")

    implementation("com.github.papsign:Ktor-OpenAPI-Generator:0.2-beta.20")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.9.8")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.9.8")
    implementation("org.webjars:swagger-ui:3.25.0")
    implementation("org.reflections:reflections:0.9.11")

    implementation("at.favre.lib:bcrypt:0.9.0")

    implementation("org.postgresql:postgresql:42.7.0")

    implementation("org.jetbrains.exposed:exposed:0.17.13")
    implementation("com.zaxxer:HikariCP:2.7.8")
    implementation("org.flywaydb:flyway-core:8.5.13")

    implementation("com.squareup.retrofit2:retrofit:2.3.0")
    implementation("com.squareup.retrofit2:converter-jackson:2.3.0")
    implementation("com.squareup.retrofit2:adapter-rxjava2:2.3.0")
    implementation("com.squareup.okhttp3:logging-interceptor:3.10.0")

    testImplementation("io.ktor:ktor-server-test-host:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlin_version")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
    testImplementation("org.assertj:assertj-core:3.22.0")
    testImplementation("io.rest-assured:rest-assured:4.5.1")

}

tasks.withType<Jar> {
    manifest {
        attributes(
            mapOf(
                "Main-Class" to application.mainClass.get()
            )
        )
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.freeCompilerArgs += "-Xuse-experimental=kotlin.ExperimentalStdlibApi"
    kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
}