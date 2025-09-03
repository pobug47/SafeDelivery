package com.gw.safedelivery

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.gw.safedelivery.model.BusinessRow
import com.gw.safedelivery.network.RetrofitClient
import com.gw.safedelivery.ui.dialog.StoreNameInputDialog
import com.gw.safedelivery.ui.dialog.ViolationDialog
import kotlinx.coroutines.launch

class ShareReceiverActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var result by remember { mutableStateOf("조회 준비 중...") }
            var showInput by remember { mutableStateOf(true) }
            var businessList by remember { mutableStateOf<List<BusinessRow>>(emptyList()) }
            var selectedLicense by remember { mutableStateOf<String?>(null) }

            when {
                showInput -> {
                    StoreNameInputDialog(
                        onConfirm = { input ->
                            showInput = false
                            fetchBusinesses(input) { list ->
                                businessList = list
                                if (list.isEmpty()) {
                                    result = "해당 업소를 찾을 수 없습니다."
                                }
                            }
                        },
                        onDismiss = { finish() }
                    )
                }

                businessList.isNotEmpty() && selectedLicense == null -> {
                    BusinessListScreen(
                        businesses = businessList,
                        onSelect = { row ->
                            selectedLicense = row.LCNS_NO
                            fetchViolations(row.LCNS_NO ?: "") { result = it }
                        }
                    )
                }

                selectedLicense != null -> {
                    ViolationDialog(result = result, onClose = { finish() })
                }

                else -> {
                    ViolationDialog(result = result, onClose = { finish() })
                }
            }
        }
    }

    /** 업소명 → 업소 리스트 조회 */
    private fun fetchBusinesses(storeName: String, onResult: (List<BusinessRow>) -> Unit) {
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.getBusinessInfo(
                    apiKey = BuildConfig.API_KEY,
                    params = "BSSH_NM=$storeName&INDUTY_CD_NM=일반음식점"
                )
                onResult(res.body()?.I2500?.row ?: emptyList())
            } catch (e: Exception) {
                Log.e("API_ERROR", "업소 조회 실패", e)
                onResult(emptyList())
            }
        }
    }

    /** 인허가번호 → 행정처분 조회 */
    private fun fetchViolations(licenseNo: String, onResult: (String) -> Unit) {
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.getViolations(
                    apiKey = BuildConfig.API_KEY,
                    params = "LCNS_NO=$licenseNo"
                )
                val match = res.body()?.I2630?.row?.firstOrNull()
                onResult(
                    if (match != null) {
                        """
                        ✅ 행정처분 내역
                        -----------------------------
                        업소명: ${match.PRCSCITYPOINT_BSSHNM}
                        업종: ${match.INDUTY_CD_NM}
                        주소: ${match.ADDR}
                        인허가번호: ${match.LCNS_NO}
                        확정일자: ${match.DSPS_DCSNDT}
                        위반내용: ${match.VILTCN}
                        처분유형: ${match.DSPS_TYPECD_NM}
                        기간: ${match.DSPS_BGNT} ~ ${match.DSPS_ENDDT}
                        """.trimIndent()
                    } else {
                        "위반 이력이 없습니다."
                    }
                )
            } catch (e: Exception) {
                Log.e("API_ERROR", "행정처분 조회 실패", e)
                onResult("조회 실패: ${e.message}")
            }
        }
    }
}

/** 업소 리스트 화면 */
@Composable
fun BusinessListScreen(businesses: List<BusinessRow>, onSelect: (BusinessRow) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("검색된 업소 목록", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn {
            items(businesses) { row ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                        .clickable { onSelect(row) }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("업소명: ${row.BSSH_NM ?: "-"}")
                        Text("주소: ${row.ADDR ?: "-"}")
                    }
                }
            }
        }
    }
}
