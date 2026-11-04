# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/4.0.9/gradle-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/4.0.9/gradle-plugin/packaging-oci-image.html)
* [Spring Boot Testcontainers support](https://docs.spring.io/spring-boot/4.0.9/reference/testing/testcontainers.html#testing.testcontainers)
* [Testcontainers Postgres Module Reference Guide](https://java.testcontainers.org/modules/databases/postgres/)
* [Spring Boot DevTools](https://docs.spring.io/spring-boot/4.0.9/reference/using/devtools.html)
* [Eureka Server](https://docs.spring.io/spring-cloud-netflix/reference/spring-cloud-netflix.html#spring-cloud-eureka-server)
* [Config Server](https://docs.spring.io/spring-cloud-config/reference/server.html)
* [Spring Reactive Web](https://docs.spring.io/spring-boot/4.0.9/reference/web/reactive.html)
* [Spring Web](https://docs.spring.io/spring-boot/4.0.9/reference/web/servlet.html)
* [Gateway](https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway-server-webmvc.html)
* [Reactive Gateway](https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway-server-webflux.html)
* [Spring Boot Actuator](https://docs.spring.io/spring-boot/4.0.9/reference/actuator/index.html)
* [Cloud LoadBalancer](https://docs.spring.io/spring-cloud-commons/reference/spring-cloud-commons/loadbalancer.html)
* [Resilience4J](https://docs.spring.io/spring-cloud-circuitbreaker/reference/spring-cloud-circuitbreaker-resilience4j.html)
* [Eureka Discovery Client](https://docs.spring.io/spring-cloud-netflix/reference/spring-cloud-netflix.html#_service_discovery_eureka_clients)
* [Spring Data JPA](https://docs.spring.io/spring-boot/4.0.9/reference/data/sql.html#data.sql.jpa-and-spring-data)
* [Validation](https://docs.spring.io/spring-boot/4.0.9/reference/io/validation.html)
* [SpringDoc OpenAPI](https://springdoc.org/)
* [Testcontainers](https://java.testcontainers.org/)
* [Liquibase Migration](https://docs.spring.io/spring-boot/4.0.9/how-to/data-initialization.html#howto.data-initialization.migration-tool.liquibase)
* [Config Client](https://docs.spring.io/spring-cloud-config/reference/client.html)
* [R2DBC API](https://docs.spring.io/spring-boot/4.0.9/reference/data/sql.html#data.sql.r2dbc)
* [OpenFeign](https://docs.spring.io/spring-cloud-openfeign/reference/)
* [Stubborn Contract Stub Runner](https://docs.stubborn.sh/contract/reference/stub-runner.html)

### Guides
The following guides illustrate how to use some features concretely:

* [Service Registration and Discovery with Eureka and Spring Cloud](https://spring.io/guides/gs/service-registration-and-discovery/)
* [Centralized Configuration](https://spring.io/guides/gs/centralized-configuration/)
* [Building a Reactive RESTful Web Service](https://spring.io/guides/gs/reactive-rest-service/)
* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [Using Spring Cloud Gateway](https://github.com/spring-cloud-samples/spring-cloud-gateway-sample)
* [Building a RESTful Web Service with Spring Boot Actuator](https://spring.io/guides/gs/actuator-service/)
* [Client-side load-balancing with Spring Cloud LoadBalancer](https://spring.io/guides/gs/spring-cloud-loadbalancer/)
* [Service Registration and Discovery with Eureka and Spring Cloud](https://spring.io/guides/gs/service-registration-and-discovery/)
* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)
* [Validation](https://spring.io/guides/gs/validating-form-input/)
* [SpringDoc OpenAPI](https://github.com/springdoc/springdoc-openapi-demos/)

### Additional Links
These additional references should also help you:

* [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)
* [R2DBC Homepage](https://r2dbc.io)
* [Declarative REST calls with Spring Cloud OpenFeign sample](https://github.com/spring-cloud-samples/feign-eureka)

### Testcontainers support

This project uses [Testcontainers at development time](https://docs.spring.io/spring-boot/4.0.9/reference/features/dev-services.html#features.dev-services.testcontainers).

Testcontainers has been configured to use the following Docker images:

* [`postgres:latest`](https://hub.docker.com/_/postgres)

Please review the tags of the used images and set them to the same as you're running in production.

