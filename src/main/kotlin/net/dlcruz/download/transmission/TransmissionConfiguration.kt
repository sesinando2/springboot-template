package net.dlcruz.download.transmission

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TransmissionConfiguration(
    private val objectMapper: ObjectMapper,
    private val configProperties: TransmissionConfigProperties
) {

    @Bean
    fun transmissionClient() = TransmissionClient(objectMapper, configProperties)
}