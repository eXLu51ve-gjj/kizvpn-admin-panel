package com.kizvpn.admin.data.api

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.kizvpn.admin.data.model.*
import java.lang.reflect.Type

/**
 * JsonDeserializer для обработки разных форматов ответов API
 * API может возвращать либо массив, либо объект с полями users/data
 */

class UsersResponseDeserializer : JsonDeserializer<UsersResponse> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): UsersResponse {
        if (json == null || context == null) {
            return UsersResponse()
        }
        
        return try {
            when {
                json.isJsonArray -> {
                    val array = json.asJsonArray
                    if (array.size() == 0) {
                        return UsersResponse(users = emptyList())
                    }
                    
                    // Проверяем, что первый элемент - объект
                    val firstElement = array[0]
                    if (firstElement.isJsonObject) {
                        val users: List<User> = context.deserialize(json, object : TypeToken<List<User>>() {}.type)
                        UsersResponse(users = users)
                    } else {
                        UsersResponse(users = emptyList())
                    }
                }
                json.isJsonObject -> {
                    val obj = json.asJsonObject
                    when {
                        obj.has("users") -> {
                            val usersElement = obj.get("users")
                            if (usersElement.isJsonArray) {
                                val users: List<User> = context.deserialize(usersElement, object : TypeToken<List<User>>() {}.type)
                                UsersResponse(users = users)
                            } else {
                                UsersResponse(users = emptyList())
                            }
                        }
                        obj.has("data") -> {
                            val dataElement = obj.get("data")
                            if (dataElement.isJsonArray) {
                                val users: List<User> = context.deserialize(dataElement, object : TypeToken<List<User>>() {}.type)
                                UsersResponse(data = users)
                            } else {
                                UsersResponse(data = emptyList())
                            }
                        }
                        else -> UsersResponse()
                    }
                }
                else -> UsersResponse()
            }
        } catch (e: Exception) {
            UsersResponse(users = emptyList())
        }
    }
}

class InboundsResponseDeserializer : JsonDeserializer<InboundsResponse> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): InboundsResponse {
        if (json == null || context == null) {
            return InboundsResponse()
        }
        
        return try {
            when {
                json.isJsonArray -> {
                    val array = json.asJsonArray
                    if (array.size() == 0) {
                        return InboundsResponse(inbounds = emptyList())
                    }
                    
                    // Проверяем, что первый элемент - объект
                    val firstElement = array[0]
                    if (firstElement.isJsonObject) {
                        val inbounds: List<Inbound> = context.deserialize(json, object : TypeToken<List<Inbound>>() {}.type)
                        InboundsResponse(inbounds = inbounds)
                    } else {
                        // Если это не объекты, возвращаем пустой список
                        InboundsResponse(inbounds = emptyList())
                    }
                }
                json.isJsonObject -> {
                    val obj = json.asJsonObject
                    when {
                        obj.has("inbounds") -> {
                            val inboundsElement = obj.get("inbounds")
                            if (inboundsElement.isJsonArray) {
                                val inbounds: List<Inbound> = context.deserialize(inboundsElement, object : TypeToken<List<Inbound>>() {}.type)
                                InboundsResponse(inbounds = inbounds)
                            } else {
                                InboundsResponse(inbounds = emptyList())
                            }
                        }
                        obj.has("data") -> {
                            val dataElement = obj.get("data")
                            if (dataElement.isJsonArray) {
                                val inbounds: List<Inbound> = context.deserialize(dataElement, object : TypeToken<List<Inbound>>() {}.type)
                                InboundsResponse(data = inbounds)
                            } else {
                                InboundsResponse(data = emptyList())
                            }
                        }
                        else -> InboundsResponse()
                    }
                }
                else -> InboundsResponse()
            }
        } catch (e: Exception) {
            // Если не удалось распарсить - возвращаем пустой список
            InboundsResponse(inbounds = emptyList())
        }
    }
}

class NodesResponseDeserializer : JsonDeserializer<NodesResponse> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): NodesResponse {
        if (json == null || context == null) {
            return NodesResponse()
        }
        
        return try {
            when {
                json.isJsonArray -> {
                    val array = json.asJsonArray
                    if (array.size() == 0) {
                        return NodesResponse(nodes = emptyList())
                    }
                    
                    // Проверяем, что первый элемент - объект
                    val firstElement = array[0]
                    if (firstElement.isJsonObject) {
                        val nodes: List<Node> = context.deserialize(json, object : TypeToken<List<Node>>() {}.type)
                        NodesResponse(nodes = nodes)
                    } else {
                        NodesResponse(nodes = emptyList())
                    }
                }
                json.isJsonObject -> {
                    val obj = json.asJsonObject
                    when {
                        obj.has("nodes") -> {
                            val nodesElement = obj.get("nodes")
                            if (nodesElement.isJsonArray) {
                                val nodes: List<Node> = context.deserialize(nodesElement, object : TypeToken<List<Node>>() {}.type)
                                NodesResponse(nodes = nodes)
                            } else {
                                NodesResponse(nodes = emptyList())
                            }
                        }
                        obj.has("data") -> {
                            val dataElement = obj.get("data")
                            if (dataElement.isJsonArray) {
                                val nodes: List<Node> = context.deserialize(dataElement, object : TypeToken<List<Node>>() {}.type)
                                NodesResponse(data = nodes)
                            } else {
                                NodesResponse(data = emptyList())
                            }
                        }
                        else -> NodesResponse()
                    }
                }
                else -> NodesResponse()
            }
        } catch (e: Exception) {
            NodesResponse(nodes = emptyList())
        }
    }
}
