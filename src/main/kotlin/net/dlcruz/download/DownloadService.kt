package net.dlcruz.download

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import net.dlcruz.download.Download.Status
import net.dlcruz.download.transmission.GetFields
import net.dlcruz.download.transmission.RpcResponse
import net.dlcruz.download.transmission.TransmissionClient
import reactor.core.publisher.toFlux
import reactor.core.publisher.toMono

class DownloadService(private val transmissionClient: TransmissionClient) {

    fun list() = transmissionClient.list().flatMapMany(::extractDownloadList)

    private fun extractDownloadList(rpcResponse: RpcResponse) =
        rpcResponse.toMono()
            .filter(RpcResponse::successful)
            .map { it.arguments.path("torrents") }
            .map(ArrayNode::class.java::cast)
            .flatMapMany(Iterable<JsonNode>::toFlux)
            .map(::toDownload)

    private fun toDownload(node: JsonNode): Download {
        val totalSize = node.get(GetFields.TOTAL_SIZE).asLong()
        val leftUntilDone = node.get(GetFields.LEFT_UNTIL_DONE).asLong()
        val downloaded = totalSize - leftUntilDone

        return Download(
            id = node.get(GetFields.HASH_STRING).asText(),
            name = node.get(GetFields.NAME).asText(),
            status = getStatus(node),
            percentDone = node.get(GetFields.PERCENT_DONE).asDouble(),
            uploadRate = node.get(GetFields.RATE_UPLOAD).asLong(),
            downloadRate = node.get(GetFields.RATE_DOWNLOAD).asLong(),
            downloaded = downloaded,
            totalSize = totalSize
        )
    }

    private fun getStatus(node: JsonNode): Status {
        val status = node.get(GetFields.STATUS).asInt()
        return when(status) {
            0 -> Status.STOPPED
            1, 2 -> Status.CHECK
            3, 4 -> Status.DOWNLOAD
            5, 6 -> Status.SEED
            else -> throw IllegalArgumentException("Unknown status: $status")
        }
    }
}
