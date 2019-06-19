package net.dlcruz.download

import net.dlcruz.download.transmission.TransmissionClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DownloadConfiguration {

    @Bean
    fun downloadService(transmissionClient: TransmissionClient) = DownloadService(transmissionClient)
}