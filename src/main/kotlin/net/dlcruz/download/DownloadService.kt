package net.dlcruz.download

import net.dlcruz.download.transmission.TransmissionClient
import reactor.core.publisher.Flux

class DownloadService(
    private val transmissionClient: TransmissionClient
) {

    fun list(): Flux<Download> {
        transmissionClient.list()
        return Flux.empty()
    }
}