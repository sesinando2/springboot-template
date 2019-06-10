package net.dlcruz.download.transmission

import com.fasterxml.jackson.databind.ObjectMapper
import net.dlcruz.config.FunctionalTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import java.util.regex.Pattern

@FunctionalTest
class TransmissionIntegrationTest {

    @Value("\${transmission.url}")
    private lateinit var url: String

    private lateinit var client: WebTestClient
    private lateinit var mapper: ObjectMapper

    private val magnetLink = "magnet:?xt=urn:btih:JZHO2UFYGJZOFIQD5VECE7S5OAIZVVGY&tr=http://nyaa.tracker.wf:7777/announce&tr=udp://tracker.coppersurfer.tk:6969/announce&tr=udp://tracker.internetwarriors.net:1337/announce&tr=udp://tracker.leechersparadise.org:6969/announce&tr=udp://tracker.opentrackr.org:1337/announce&tr=udp://open.stealth.si:80/announce&tr=udp://p4p.arenabg.com:1337/announce&tr=udp://mgtracker.org:6969/announce&tr=udp://tracker.tiny-vps.com:6969/announce&tr=udp://peerfect.org:6969/announce&tr=http://share.camoe.cn:8080/announce&tr=http://t.nyaatracker.com:80/announce&tr=https://open.kickasstracker.com:443/announce"

    @BeforeEach
    fun setup() {
        client = WebTestClient.bindToServer().baseUrl(url).build()
        mapper = ObjectMapper()
    }

    @Test
    fun `test rpc calls`() {
        val addTorrentRequest = mapper.createObjectNode().put("method", "torrent-add")
        addTorrentRequest.putObject("arguments").put("filename", magnetLink)

        val initialResponse = client.post()
            .body(BodyInserters.fromObject(addTorrentRequest))
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.CONFLICT)
            .expectBody(String::class.java)
            .returnResult()

        val pattern = Pattern.compile(""".*<code>X-Transmission-Session-Id: (\w+)<\/code>.*""")
        val responseBody = initialResponse.responseBody
        assertThat(responseBody).containsPattern(pattern)

        val matcher = pattern.matcher(responseBody)
        assertThat(matcher.matches()).isTrue()
        assertThat(matcher.group(1)).isNotNull()
        assertThat(matcher.group(1)).isNotEmpty()

        val sessionId = matcher.group(1)

        client.post()
            .header("X-Transmission-Session-Id", sessionId)
            .body(BodyInserters.fromObject(addTorrentRequest))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.result").isEqualTo("success")
            .jsonPath("$.arguments.torrent-added.id").exists()
            .jsonPath("$.arguments.torrent-added.name").exists()
            .jsonPath("$.arguments.torrent-added.hashString").exists()

        val result = client.post()
            .header("X-Transmission-Session-Id", sessionId)
            .body(BodyInserters.fromObject(addTorrentRequest))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.result").isEqualTo("success")
            .jsonPath("$.arguments.torrent-duplicate.id").exists()
            .jsonPath("$.arguments.torrent-duplicate.name").exists()
            .jsonPath("$.arguments.torrent-duplicate.hashString").exists()
            .returnResult()

        val resultJson = mapper.readTree(result.responseBody)
        val id = resultJson.path("arguments").path("torrent-duplicate").path("id").asLong()
        val name = resultJson.path("arguments").path("torrent-duplicate").path("name").asText()
        val hashString = resultJson.path("arguments").path("torrent-duplicate").path("hashString").asText()

        val getTorrentRequest = mapper.createObjectNode().put("method", "torrent-get")
        val getArguments = getTorrentRequest.putObject("arguments")
        getArguments.putArray("ids").add(id)
        getArguments.putArray("fields").add("id").add("name").add("hashString")

        client.post()
            .header("X-Transmission-Session-Id", sessionId)
            .body(BodyInserters.fromObject(getTorrentRequest))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.result").isEqualTo("success")
            .jsonPath("$.arguments.torrents[?(@.id == $id)]").exists()
            .jsonPath("$.arguments.torrents[?(@.name == '$name')]").exists()
            .jsonPath("$.arguments.torrents[?(@.hashString == '$hashString')]").exists()

        val deleteTorrentRequest = mapper.createObjectNode().put("method", "torrent-remove")
        val deleteArguments = deleteTorrentRequest.putObject("arguments").put("delete-local-data", true)
        deleteArguments.putArray("ids").add(id)

        client.post()
            .header("X-Transmission-Session-Id", sessionId)
            .body(BodyInserters.fromObject(deleteTorrentRequest))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.result").isEqualTo("success")
            .jsonPath("$.arguments").isEmpty
    }
}