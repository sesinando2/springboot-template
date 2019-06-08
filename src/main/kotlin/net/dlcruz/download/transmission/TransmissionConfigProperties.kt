package net.dlcruz.download.transmission

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("transmission")
class TransmissionConfigProperties {

    lateinit var url: String
}