plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.serialization)
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation(libs.ktor.client.mock)

    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.cbor)

    implementation(libs.ktor.server.core.jvm)
    implementation(libs.ktor.serialization.kotlinx.json.jvm)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.content.negotiation.jvm)
    implementation(libs.ktor.client.content.negotiation)

    implementation(libs.ktor.client.cio)

    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.json)
    implementation(libs.exposed.kotlin.datetime)

    implementation(libs.ktor.server.auth.jvm)
    implementation(libs.ktor.server.auth.jwt.jvm)
    implementation(libs.ktor.server.netty.jvm)
    implementation(libs.ktor.server.html.builder)
    implementation(libs.ktor.server.websockets)
    implementation(libs.ktor.server.config.yaml)
    implementation(libs.ktor.server.cors)

    implementation(libs.logback.classic)

    implementation("org.postgresql:postgresql:42.7.3")
    implementation("com.pgvector:pgvector:0.1.6")

    implementation(libs.exposed.migration)
    implementation("org.flywaydb:flyway-core:11.7.0")
    implementation("org.flywaydb:flyway-database-postgresql:11.7.0")

    implementation("com.fleeksoft.ksoup:ksoup:0.2.5")

    implementation(project(":kabinet"))
}
