package com.kizvpn.admin.data.api

import com.kizvpn.admin.data.model.*
import retrofit2.http.*

interface PasarGuardApi {
    
    // System
    @GET("system")
    suspend fun getSystemInfo(): SystemInfo
    
    // Users
    @GET("users")
    suspend fun getUsers(): UsersResponse
    
    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: Int): User
    
    @POST("users")
    suspend fun createUser(@Body user: CreateUserRequest): User
    
    @POST("user")
    suspend fun createUserSingular(@Body user: CreateUserRequest): User
    
    @PUT("users/{id}")
    suspend fun updateUser(@Path("id") id: Int, @Body user: UpdateUserRequest): User
    
    @PUT("user/{id}")
    suspend fun updateUserSingular(@Path("id") id: Int, @Body user: UpdateUserRequest): User
    
    @PATCH("users/{id}")
    suspend fun patchUser(@Path("id") id: Int, @Body user: UpdateUserRequest): User
    
    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") id: Int)
    
    @DELETE("user/{id}")
    suspend fun deleteUserSingular(@Path("id") id: Int)
    
    @GET("users/{id}/subscription")
    suspend fun getUserSubscription(@Path("id") id: Int): Map<String, String>
    
    @GET("user/{id}/subscription")
    suspend fun getUserSubscriptionSingular(@Path("id") id: Int): Map<String, String>
    
    @GET("users/{id}/config")
    suspend fun getUserConfig(@Path("id") id: Int): Map<String, Any>
    
    @GET("user/{id}/config")
    suspend fun getUserConfigSingular(@Path("id") id: Int): Map<String, Any>
    
    @GET("users/{id}/stats")
    suspend fun getUserStats(@Path("id") id: Int): UserStats
    
    // Inbounds
    @GET("inbounds")
    suspend fun getInbounds(): InboundsResponse
    
    @GET("inbounds/{id}")
    suspend fun getInbound(@Path("id") id: Int): Inbound
    
    // Nodes
    @GET("nodes")
    suspend fun getNodes(): NodesResponse
    
    @GET("nodes/{id}")
    suspend fun getNode(@Path("id") id: Int): Node
    
    // Stats
    @GET("stats")
    suspend fun getStats(): Stats
    
    @GET("stats/users")
    suspend fun getUsersStats(): Map<String, Any>
    
    @GET("stats/nodes")
    suspend fun getNodesStats(): Map<String, Any>
}


