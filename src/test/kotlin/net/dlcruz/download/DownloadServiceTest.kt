package net.dlcruz.download

import net.dlcruz.config.TransmissionStub
import net.dlcruz.download.Download.Status
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import reactor.test.StepVerifier

@TransmissionStub
class DownloadServiceTest(
    @Autowired
    private val downloadService: DownloadService
) {

    @Test
    fun `should return download list`() {
        val result = downloadService.list()

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