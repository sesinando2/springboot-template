package net.dlcruz.download

data class Download(
    val id: String,
    val name: String,
    val status: Status,
    val percentDone: Double,
    val downloadRate: Long,
    val uploadRate: Long,
    val downloaded: Long,
    val totalSize: Long
) {

    enum class Status {
        STOPPED, CHECK, DOWNLOAD, SEED, ERROR
    }
}