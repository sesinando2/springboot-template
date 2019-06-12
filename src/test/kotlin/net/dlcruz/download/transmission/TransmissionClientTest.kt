package net.dlcruz.download.transmission

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.* // ktlint-disable no-wildcard-imports
import net.dlcruz.config.TestConfiguration
import net.dlcruz.util.TestHelper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import reactor.test.StepVerifier
import java.io.InputStreamReader

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
        val producer = client.list()

        val responseResource = TestHelper.readAsStream("get-torrent-response.json").readAllBytes()
        val expectedResponse = mapper.readValue<RpcResponse>(responseResource, RpcResponse::class.java)

        val requestResource = TestHelper.readAsStream("get-torrent-request.json")
        val expectedRequest = requestResource.reader().use(InputStreamReader::readText)

        StepVerifier.create(producer).expectNext(expectedResponse).expectComplete().verify()

        verify(1, postRequestedFor(urlEqualTo("/transmission/rpc"))
            .withoutHeader("X-Transmission-Session-Id")
            .withRequestBody(equalToJson(expectedRequest)))

        verify(1, postRequestedFor(urlEqualTo("/transmission/rpc"))
            .withHeader("X-Transmission-Session-Id", equalTo("TlTPAGWbQttbSL9RT7rXDTj1ir81J0tbnVUm0zeBnqBFlWNQ"))
            .withRequestBody(equalToJson(expectedRequest)))
    }
}