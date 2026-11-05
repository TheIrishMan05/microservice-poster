rootProject.name = "microservice-poster"

listOf(
    "shared-contracts",
    "eureka-server",
    "config-server",
    "api-gateway",
    "user-service",
    "event-service",
    "order-service"
).forEach { name ->
    if (file(name).exists()) {
        include(name)
    }
}
