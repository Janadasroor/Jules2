package com.jnd.jules.network

import com.jnd.jules.model.ListSessionsResponse
import com.jnd.jules.model.Session
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface JulesApiService {
    @GET("v1alpha/sessions")
    suspend fun listSessions(
        @Query("pageSize") pageSize: Int = 10,
        @Query("pageToken") pageToken: String? = null
    ): ListSessionsResponse

    @POST("v1alpha/sessions")
    suspend fun createSession(@Body session: Session): Session

    @GET("v1alpha/sessions/{id}")
    suspend fun getSession(@retrofit2.http.Path("id") id: String): Session

    @POST("v1alpha/{session}:sendMessage")
    suspend fun sendMessage(
        @retrofit2.http.Path("session", encoded = true) sessionName: String,
        @Body request: com.jnd.jules.model.SendMessageRequest
    )

    @GET("v1alpha/sources")
    suspend fun listSources(
        @Query("pageSize") pageSize: Int = 10,
        @Query("pageToken") pageToken: String? = null
    ): com.jnd.jules.model.ListSourcesResponse

    @POST("v1alpha/{session}:approvePlan")
    suspend fun approvePlan(@retrofit2.http.Path("session", encoded = true) sessionName: String)

    @GET("v1alpha/{session}/activities")
    suspend fun listActivities(
        @retrofit2.http.Path("session", encoded = true) sessionName: String,
        @Query("pageSize") pageSize: Int = 50,
        @Query("pageToken") pageToken: String? = null
    ): com.jnd.jules.model.ListActivitiesResponse

    @GET("v1alpha/{name}")
    suspend fun getActivity(
        @retrofit2.http.Path("name", encoded = true) name: String
    ): com.jnd.jules.model.Activity

    @GET("v1alpha/{name}")
    suspend fun getSource(
        @retrofit2.http.Path("name", encoded = true) name: String
    ): com.jnd.jules.model.Source
}
