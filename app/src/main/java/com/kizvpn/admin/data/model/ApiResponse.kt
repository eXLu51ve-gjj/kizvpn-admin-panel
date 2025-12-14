package com.kizvpn.admin.data.model

import com.google.gson.annotations.SerializedName

// Обертки для ответов API, которые возвращают объекты вместо массивов

data class UsersResponse(
    val users: List<User>? = null,
    val data: List<User>? = null
) {
    fun getUsersList(): List<User> {
        return users ?: data ?: emptyList()
    }
}

data class InboundsResponse(
    val inbounds: List<Inbound>? = null,
    val data: List<Inbound>? = null
) {
    fun getInboundsList(): List<Inbound> {
        return inbounds ?: data ?: emptyList()
    }
}

data class NodesResponse(
    val nodes: List<Node>? = null,
    val data: List<Node>? = null
) {
    fun getNodesList(): List<Node> {
        return nodes ?: data ?: emptyList()
    }
}

