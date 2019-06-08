package net.dlcruz

import net.dlcruz.download.transmission.TransmissionConfigProperties
import net.dlcruz.logging.LoggerDelegate
import org.apache.commons.lang3.StringUtils
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.info.BuildProperties
import org.springframework.boot.info.GitProperties
import org.springframework.boot.runApplication
import org.springframework.context.event.EventListener
import org.springframework.core.env.Environment
import java.net.InetAddress
import java.util.* // ktlint-disable no-wildcard-imports

@EnableConfigurationProperties(TransmissionConfigProperties::class)
@SpringBootApplication
class Application(
    private val environment: Environment,
    private val buildProperties: BuildProperties,
    private val gitProperties: GitProperties
) {

    private val log by LoggerDelegate()

    @EventListener(ApplicationReadyEvent::class)
    fun startup() {
        val hostAddress = try {
            InetAddress.getLocalHost().hostAddress
        } catch (e: Exception) {
            log.warn("The host name could not be determined, using `localhost` as fallback")
            "localhost"
        }

        val format = '\n'.toString() +
                "--------------------------------------------------------------------------------------\n" +
                "    Application %2\$s v%8\$s (%9\$s) is running! Access URLs:\n" +
                "    Local               : %1\$s://localhost:%3\$s%5\$s\n" +
                "    ApiDoc              : %1\$s://localhost:%3\$s%5\$s/swagger-ui.html \n" +
                "    Management          : %1\$s://localhost:%4\$s%5\$s/actuator\n" +
                "    Available Resources : %1\$s://localhost:%4\$s%5\$s/actuator/mappings\n" +
                "    External            : %1\$s://%6\$s:%4\$s%5\$s\n" +
                "    Profile(s)          : %7\$s\n" +
                "--------------------------------------------------------------------------------------"
        log.info(String.format(
                format,
                if (environment.getProperty("server.ssl.key-store") == null) "http" else "https",
                StringUtils.defaultIfBlank(buildProperties.name, "No Name"),
                environment.getProperty("server.port"),
                StringUtils.defaultIfBlank(environment.getProperty("management.server.port"), environment.getProperty("server.port")),
                StringUtils.defaultIfBlank(environment.getProperty("server.servlet.context-path"), ""),
                hostAddress,
                Arrays.toString(environment.activeProfiles),
                StringUtils.defaultIfBlank(buildProperties.version, "NoVersion"),
                StringUtils.defaultIfBlank(gitProperties.shortCommitId, "NoCommitId")
        ))
    }
}

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
