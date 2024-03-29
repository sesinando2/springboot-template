package net.dlcruz.sample

import io.swagger.annotations.Api
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.* // ktlint-disable no-wildcard-imports
import javax.validation.Valid

@Api(tags = ["sample"], description = "Sample Controller")
@RestController
@RequestMapping("/sample")
class SampleController(private val sampleService: SampleService) {

    @PostMapping
    fun post(@Valid @RequestBody sample: Sample) =
        sampleService
            .create(sample)
            .map { ResponseEntity(it, HttpStatus.CREATED) }

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long) =
        sampleService
            .get(id)
            .map { ResponseEntity(it, HttpStatus.OK) }

    @PutMapping("/{id}")
    fun put(
        @PathVariable id: Long,
        @Valid @RequestBody sample: Sample
    ) =
        sampleService
            .update(id, sample)
            .map { ResponseEntity(it, HttpStatus.OK) }
}