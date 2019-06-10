package net.dlcruz.download.transmission

import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.collect.ImmutableList

data class RpcRequest(val method: String, val arguments: ObjectNode)

object Method {
    const val TORRENT_ADD = "torrent-add"
    const val TORRENT_GET = "torrent-get"
    const val TORRENT_REMOVE = "torrent-remove"
}

object GetArguments {
    const val IDS = "ids"
    const val FIELDS = "fields"
}

object GetFields {
    const val ID = "id"
    const val NAME = "name"
    const val TOTAL_SIZE = "totalSize"
    const val ETA = "eta"
    const val ETA_IDLE = "etaIdle"
    const val STATUS = "status"
    const val QUEUE_POSITION = "queuePosition"
    const val ERROR = "error"
    const val ERROR_STRING = "errorString"
    const val LEFT_UNTIL_DONE = "leftUntilDone"
    const val PERCENT_DONE = "percentDone"
    const val RATE_DOWNLOAD = "rateDownload"
    const val RATE_UPLOAD = "rateUpload"
    const val SECONDS_DOWNLOADING = "secondsDownloading"
    const val SECONDS_SEEDING = "secondsSeeding"
    const val HASH_STRING = "hashString"

    val all = ImmutableList.of(ID, NAME, TOTAL_SIZE, ETA, ETA_IDLE,
        STATUS, QUEUE_POSITION, ERROR, ERROR_STRING, LEFT_UNTIL_DONE, PERCENT_DONE,
        RATE_DOWNLOAD, RATE_UPLOAD, SECONDS_DOWNLOADING, SECONDS_SEEDING, HASH_STRING)
}