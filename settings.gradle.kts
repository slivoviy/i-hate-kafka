rootProject.name = "streaming-service"

include(
    "api",
    "orchestrator",
    "runner",
    "inference",
)

pluginManagement {
    repositories {
        mavenCentral()
        maven("https://packages.confluent.io/maven/")
    }
}