package com.gw.safedelivery.model

// 인허가 조회 API (I2500) 응답 구조
data class BusinessResponse(
    val I2500: BusinessData?
)

data class BusinessData(
    val total_count: String?,
    val row: List<BusinessRow>?
)

data class BusinessRow(
    val LCNS_NO: String?,    // 인허가번호
    val BSSH_NM: String?,    // 업소명
    val ADDR: String?        // 주소
)
