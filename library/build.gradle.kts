plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.serialization)
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test"))
}
