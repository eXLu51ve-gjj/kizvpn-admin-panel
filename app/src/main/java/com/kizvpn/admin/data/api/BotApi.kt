package com.kizvpn.admin.data.api

import com.kizvpn.admin.data.model.*
import retrofit2.http.*

/**
 * API для работы с PostgreSQL БД через бот-сервер
 * REST API endpoint на сервере bot_api_server.py
 */
interface BotApi {
    
    @GET("api/subscription/{vpn_user_id}")
    suspend fun getSubscriptionUrl(@Path("vpn_user_id") vpnUserId: Int): SubscriptionUrlResponse
    
    @GET("api/subscription/by-username/{username}")
    suspend fun getSubscriptionUrlByUsername(@Path("username") username: String): SubscriptionUrlResponse
    
    @GET("api/payments")
    suspend fun getPayments(
        @Query("status") status: String? = null,
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0
    ): List<Payment>
    
    @GET("api/payments/{id}")
    suspend fun getPayment(@Path("id") id: Int): Payment
    
    @PUT("api/payments/{id}/confirm")
    suspend fun confirmPayment(@Path("id") id: Int): Payment
    
    @GET("api/tariffs")
    suspend fun getTariffs(): List<Tariff>
    
    @GET("api/subscriptions")
    suspend fun getSubscriptions(
        @Query("user_id") userId: Int? = null,
        @Query("status") status: String? = null
    ): List<Subscription>
    
    @GET("api/subscriptions/{id}")
    suspend fun getSubscription(@Path("id") id: Int): Subscription
    
    @POST("api/server/{server_ip}/reboot")
    suspend fun rebootServer(@Path("server_ip") serverIp: String): ServerRebootResponse
}

