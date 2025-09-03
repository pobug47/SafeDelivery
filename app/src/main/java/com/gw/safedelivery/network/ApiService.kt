package com.gw.safedelivery.network

import com.gw.safedelivery.model.BusinessResponse
import com.gw.safedelivery.model.ViolationResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    // 인허가 조회 (I2500)
    @GET("{apiKey}/I2500/json/{start}/{end}/{params}")
    suspend fun getBusinessInfo(
        @Path("apiKey") apiKey: String,
        @Path("start") start: Int = 1,
        @Path("end") end: Int = 5,
        @Path(value = "params", encoded = true) params: String
    ): Response<BusinessResponse>

    // 행정처분 조회 (I2630)
    @GET("{apiKey}/I2630/json/{start}/{end}/{params}")
    suspend fun getViolations(
        @Path("apiKey") apiKey: String,
        @Path("start") start: Int = 1,
        @Path("end") end: Int = 5,
        @Path(value = "params", encoded = true) params: String
    ): Response<ViolationResponse>
}
