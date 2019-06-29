package net.dlcruz.download.transmission

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.* // ktlint-disable no-wildcard-imports
import net.dlcruz.config.TestConfiguration
import net.dlcruz.util.TestHelper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import reactor.test.StepVerifier
import java.io.InputStreamReader
import java.util.* // ktlint-disable no-wildcard-imports
import java.util.stream.Stream

@SpringBootTest(classes = [TestConfiguration::class], webEnvironment = NONE)
@AutoConfigureWebClient
@AutoConfigureWireMock(port = 0, stubs = ["classpath:/stubs"])
class TransmissionClientTest(

    @Value("\${wiremock.server.port}")
    private val port: String,

    @Autowired
    private val mapper: ObjectMapper
) {

    private lateinit var client: TransmissionClient

    @BeforeEach
    fun setup() {
        val config = TransmissionConfigProperties()
        config.url = "http://localhost:$port/transmission/rpc"
        client = TransmissionClient(mapper, config)
    }

    @Test
    fun `should get list of torrent`() {
        val publisher = client.list()

        val responseResource = TestHelper.readAsStream("get-torrent-response.json").readAllBytes()
        val expectedResponse = mapper.readValue<RpcResponse>(responseResource, RpcResponse::class.java)

        val requestResource = TestHelper.readAsStream("get-torrent-request.json")
        val expectedRequest = requestResource.reader().use(InputStreamReader::readText)

        StepVerifier.create(publisher).expectNext(expectedResponse).expectComplete().verify()

        verify(1, postRequestedFor(urlEqualTo("/transmission/rpc"))
            .withHeader("X-Transmission-Session-Id", equalTo("TlTPAGWbQttbSL9RT7rXDTj1ir81J0tbnVUm0zeBnqBFlWNQ"))
            .withRequestBody(equalToJson(expectedRequest)))
    }

    @Test
    fun `should be able to add torrent`() {
        val link = "http://download-link.com"
        val publisher = client.add(link)

        val responseResource = TestHelper.readAsStream("add-torrent-response.json").readAllBytes()
        val expectedResponse = mapper.readValue<RpcResponse>(responseResource, RpcResponse::class.java)

        StepVerifier.create(publisher).expectNext(expectedResponse).expectComplete().verify()

        verify(1, postRequestedFor(urlEqualTo("/transmission/rpc"))
            .withHeader("X-Transmission-Session-Id", equalTo("TlTPAGWbQttbSL9RT7rXDTj1ir81J0tbnVUm0zeBnqBFlWNQ"))
            .withRequestBody(matchingJsonPath("$.method", equalTo("torrent-add")))
            .withRequestBody(matchingJsonPath("$.arguments.filename", equalTo(link))))
    }

    @ParameterizedTest
    @MethodSource("deleteDataProvider")
    fun `should be able to delete torrent`(id: Int, deleteLocalData: Boolean) {
        val publisher = client.delete(id, deleteLocalData = deleteLocalData)

        val responseResource = TestHelper.readAsStream("delete-torrent-response.json").readAllBytes()
        val expectedResponse = mapper.readValue<RpcResponse>(responseResource, RpcResponse::class.java)

        StepVerifier.create(publisher).expectNext(expectedResponse).expectComplete().verify()

        verify(1, postRequestedFor(urlEqualTo("/transmission/rpc"))
            .withHeader("X-Transmission-Session-Id", equalTo("TlTPAGWbQttbSL9RT7rXDTj1ir81J0tbnVUm0zeBnqBFlWNQ"))
            .withRequestBody(matchingJsonPath("$.method", equalTo("torrent-remove")))
            .withRequestBody(matchingJsonPath("$.arguments.ids[0]", equalTo("$id")))
            .withRequestBody(matchingJsonPath("$.arguments.delete-local-data", equalTo("$deleteLocalData"))))
    }

    companion object {
        @JvmStatic
        private fun deleteDataProvider() = Stream.of(
            Arguments.of(Random().nextInt(), true),
            Arguments.of(Random().nextInt(), false)
        )
    }
}