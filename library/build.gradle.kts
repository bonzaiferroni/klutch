plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.serialization)
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test"))

    implementation(libs.exposed.core)

    implementation(libs.ktor.server.core.jvm)

    implementation(project(":kabinet"))
}
