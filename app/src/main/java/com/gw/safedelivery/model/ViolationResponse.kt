package com.gw.safedelivery.model

// 행정처분 조회 API (I2630) 응답 구조
data class ViolationResponse(
    val I2630: ViolationData?
)

data class ViolationData(
    val total_count: String?,
    val row: List<ViolationRow>?
)

data class ViolationRow(
    val PRCSCITYPOINT_BSSHNM: String?, // 업소명
    val INDUTY_CD_NM: String?,         // 업종
    val ADDR: String?,                 // 주소
    val VILTCN: String?,               // 위반내역
    val DSPS_TYPECD_NM: String?,       // 처분유형
    val DSPS_BGNT: String?,            // 시작일
    val DSPS_ENDDT: String?,           // 종료일
    val LCNS_NO: String?,              // 인허가 번호
    val DSPS_DCSNDT: String?           // 처분 확정일자
)
