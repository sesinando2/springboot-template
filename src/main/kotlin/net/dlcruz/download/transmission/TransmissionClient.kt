package net.dlcruz.download.transmission

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient

class TransmissionClient(
    private val mapper: ObjectMapper,
    private val properties: TransmissionConfigProperties
) {

    var client = WebClient.builder().baseUrl(properties.url).build()

    val newGetRpcRequest: RpcRequest
        get() {
            val arguments = mapper.createObjectNode()
            val fields = arguments.putArray("fields")
            GetFields.all.forEach { fields.add(it) }
            return RpcRequest(Method.TORRENT_GET, arguments)
        }

    fun list() = client.post()
        .body(BodyInserters.fromObject(newGetRpcRequest))
        .retrieve()
        .bodyToMono(RpcResponse::class.java)
}