package net.dlcruz.download.transmission

import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock

@SpringBootTest
@AutoConfigureWireMock(port = 0, stubs = ["classpath:/stubs"])
class TransmissionClientIntegrationTest(

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
    fun `test`() {
        val response = client.list().block()
        MatcherAssert.assertThat(response, Matchers.notNullValue())
    }
}