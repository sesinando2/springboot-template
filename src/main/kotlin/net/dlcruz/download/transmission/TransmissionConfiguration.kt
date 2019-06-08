package net.dlcruz.download.transmission

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TransmissionConfiguration(
    private val transmissionConfigProperties: TransmissionConfigProperties
) {

    @Bean
    fun transmissionClient() = TransmissionClient(transmissionConfigProperties)
}