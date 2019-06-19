package net.dlcruz.download.transmission

import com.fasterxml.jackson.databind.ObjectMapper
import net.dlcruz.logging.LoggerDelegate
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono

typealias TransmissionRequest = (WebClient) -> Mono<ClientResponse>

const val HEADER_SESSION = "X-Transmission-Session-Id"

class TransmissionClient(
    private val mapper: ObjectMapper,
    private val properties: TransmissionConfigProperties
) {

    private val logger by LoggerDelegate()
    private var client = WebClient.builder().baseUrl(properties.url).build()

    fun list() = send(createGetRequest())

    fun add(link: String) = send(RpcRequest(
        method = Method.TORRENT_ADD,
        arguments = mapper.createObjectNode().put("filename", link)))

    fun delete(vararg ids: Int, deleteLocalData: Boolean) =
        send(createRemoveRequest(*ids, deleteLocalData = deleteLocalData))

    private fun send(request: RpcRequest) =
        exchange { client ->
            request.toMono()
                .doOnNext { logger.debug("Sending RPC Request: {}", mapper.writeValueAsString(it)) }
                .map { BodyInserters.fromObject(it) }
                .map { client.post().body(it) }
                .flatMap { it.exchange() }
        }

    private fun exchange(request: TransmissionRequest) =
        request(client).flatMap {
            when (it.statusCode()) {
                HttpStatus.CONFLICT -> handleConflict(it, request)
                else -> it.toMono()
            }
        }.flatMap(::toClientResponse)

    private fun toClientResponse(clientResponse: ClientResponse) =
        clientResponse.bodyToMono(RpcResponse::class.java)
            .doOnNext { logger.debug("RpcResponse: {}", mapper.writeValueAsString(it)) }

    private fun handleConflict(it: ClientResponse, request: TransmissionRequest): Mono<ClientResponse> {
        return it.bodyToMono(String::class.java).flatMap {
            logger.debug("Received 409 with body: {}", it)
            val sessionId = parseSessionId(it)
            logger.debug("Extracted Session ID: {}", sessionId)
            client = WebClient.builder().baseUrl(properties.url).defaultHeader(HEADER_SESSION, sessionId).build()
            request(client)
        }
    }

    private fun parseSessionId(response: String): String {
        return Regex(""".*<code>$HEADER_SESSION: (\w+)<\/code>.*""")
            .find(response)?.groups?.get(1)?.value
            ?: throw IllegalArgumentException("Unable to parse $HEADER_SESSION from $response")
    }

    private fun createGetRequest(vararg ids: Int): RpcRequest {
        val arguments = mapper.createObjectNode()
        val fields = arguments.putArray("fields")
        GetFields.all.forEach { fields.add(it) }
        if (ids.isNotEmpty()) {
            val idsArray = arguments.putArray("ids")
            ids.forEach { idsArray.add(it) }
        }
        return RpcRequest(Method.TORRENT_GET, arguments)
    }

    private fun createRemoveRequest(vararg ids: Int, deleteLocalData: Boolean): RpcRequest {
        val arguments = mapper.createObjectNode().put("delete-local-data", deleteLocalData)
        val idsArray = arguments.putArray("ids")
        ids.forEach { idsArray.add(it) }
        return RpcRequest(Method.TORRENT_REMOVE, arguments)
    }
}