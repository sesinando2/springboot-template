package net.dlcruz.config

import net.dlcruz.download.DownloadConfiguration
import net.dlcruz.download.transmission.TransmissionClient
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@WebFluxTest
@Import(TestConfiguration::class, TransmissionClient::class, DownloadConfiguration::class)
@AutoConfigureWireMock(port = 0, stubs = ["classpath:/stubs"])
@ActiveProfiles("test")
annotation class TransmissionStub