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

    val logger by LoggerDelegate()

    var client = WebClient.builder().baseUrl(properties.url).build()

    val newGetRpcRequest: RpcRequest
        get() {
            val arguments = mapper.createObjectNode()
            val fields = arguments.putArray("fields")
            GetFields.all.forEach { fields.add(it) }
            return RpcRequest(Method.TORRENT_GET, arguments)
        }

    fun list() = send(newGetRpcRequest)

    fun add(link: String) = send(RpcRequest(
        method = Method.TORRENT_ADD,
        arguments = mapper.createObjectNode().put("filename", link)))

    private fun send(request: RpcRequest) =
        exchange { client ->
            request.toMono()
                .doOnNext { logger.debug("Sending RPC Request: {}", mapper.writeValueAsString(it)) }
                .flatMap { client.post().body(BodyInserters.fromObject(it)).exchange() }
        }
            .flatMap { it.bodyToMono(RpcResponse::class.java) }
            .doOnNext { logger.debug("RpcResponse: {}", mapper.writeValueAsString(it)) }

    private fun exchange(request: TransmissionRequest) =
        request(client).flatMap {
            when (it.statusCode()) {
                HttpStatus.CONFLICT -> handleConflict(it, request)
                else -> it.toMono()
            }
        }

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
}