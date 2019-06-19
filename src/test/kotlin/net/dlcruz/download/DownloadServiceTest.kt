package net.dlcruz.download

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.dlcruz.config.TestConfiguration
import net.dlcruz.download.Download.*
import net.dlcruz.download.transmission.RpcResponse
import net.dlcruz.download.transmission.TransmissionClient
import net.dlcruz.util.TestHelper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.toMono
import reactor.test.StepVerifier

@SpringBootTest(classes = [TestConfiguration::class], webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWebClient
class DownloadServiceTest(
    @Autowired
    private val mapper: ObjectMapper
) {

    private lateinit var transmissionClient: TransmissionClient
    private lateinit var downloadService: DownloadService

    @BeforeEach
    fun setup() {
        transmissionClient = mockk()
        downloadService = DownloadService(transmissionClient)
    }

    @Test
    fun `should return download list`() {
        val responseResource = TestHelper.readAsStream("get-torrent-response.json").readAllBytes()
        val getTorrentResponse = mapper.readValue<RpcResponse>(responseResource, RpcResponse::class.java)

        every { transmissionClient.list() } returns getTorrentResponse.toMono()

        val result = downloadService.list()

        verify(exactly = 1) { transmissionClient.list() }

        StepVerifier.create(result)
            .expectNext(
                Download(
                    id = "4e4eed50b83272e2a203ed48227e5d70119ad4d8",
                    name = "[HorribleSubs] One Punch Man S2 - 01 [720p].mkv",
                    status = Status.DOWNLOAD,
                    percentDone = 0.5476,
                    downloadRate = 131000,
                    uploadRate = 0,
                    downloaded = 210223533,
                    totalSize = 383877549
                ),
                Download(
                    id = "40ef96f421a200db5fce88eed355982e3fa4c333",
                    name = "[HorribleSubs] Shokugeki no Soma S3 - 01 [720p].mkv",
                    status = Status.DOWNLOAD,
                    percentDone = 0.0,
                    downloadRate = 0,
                    uploadRate = 0,
                    downloaded = 0,
                    totalSize = 345971463
                ))
            .expectComplete()
            .verify()
    }
}