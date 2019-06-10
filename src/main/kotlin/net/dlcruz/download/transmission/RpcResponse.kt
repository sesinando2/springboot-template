package net.dlcruz.download.transmission

import com.fasterxml.jackson.databind.node.ObjectNode

data class RpcResponse(val result: String, val arguments: ObjectNode)