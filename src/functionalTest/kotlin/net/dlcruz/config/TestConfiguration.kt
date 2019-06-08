package net.dlcruz.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class TestConfiguration {

    private val targetUrl: String
    get() = System.getProperty("targetUrl") ?: "http://localhost:8080"

    @Bean
    fun webClient() = WebClient.builder().baseUrl(targetUrl).build()

    @Bean
    fun webTestClient() = WebTestClient.bindToServer().baseUrl(targetUrl).build()
}