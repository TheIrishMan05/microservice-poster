rootProject.name = "microservice-poster"

include(
    "shared-contracts",
    "eureka-server",
    "config-server",
    "api-gateway",
    "user-service",
    "event-service",
    "order-service"
)