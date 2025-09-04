package com.gw.safedelivery

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.gw.safedelivery.model.BusinessRow
import com.gw.safedelivery.network.RetrofitClient
import com.gw.safedelivery.ui.dialog.BusinessListDialog
import com.gw.safedelivery.ui.dialog.StoreNameInputDialog
import com.gw.safedelivery.ui.dialog.StoreUrlInputDialog
import com.gw.safedelivery.ui.dialog.ViolationDialog
import com.gw.safedelivery.util.HtmlParser
import kotlinx.coroutines.launch

class ShareReceiverActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inputMode = intent.getStringExtra("INPUT_MODE")
        val storeName = if (inputMode == null) extractStoreName(intent) else null
        val urlFromIntent = if (inputMode == null) extractHttpUrl(intent) else null

        setContent {
            var state by remember { mutableStateOf("INPUT") }
            var businessList by remember { mutableStateOf(listOf<BusinessRow>()) }
            var result by remember { mutableStateOf("") }

            when (state) {
                // 초기 상태
                "INPUT" -> {
                    when (inputMode) {
                        "NAME" -> {
                            LaunchedEffect(Unit) { state = "STORE_INPUT" }
                        }
                        "URL" -> {
                            LaunchedEffect(Unit) { state = "URL_INPUT" }
                        }
                        else -> {
                            // 공유로 들어온 경우
                            when {
                                !storeName.isNullOrEmpty() -> {
                                    LaunchedEffect(storeName) {
                                        fetchBusiness(storeName) { list ->
                                            if (list.isEmpty()) state = "STORE_INPUT"
                                            else { businessList = list; state = "LIST" }
                                        }
                                    }
                                }
                                !urlFromIntent.isNullOrEmpty() -> {
                                    LaunchedEffect(urlFromIntent) {
                                        val title = HtmlParser.extractTitle(urlFromIntent)
                                        val sanitized = title?.let { sanitizeStoreName(it) }
                                        if (!sanitized.isNullOrEmpty()) {
                                            fetchBusiness(sanitized) { list ->
                                                if (list.isEmpty()) state = "STORE_INPUT"
                                                else { businessList = list; state = "LIST" }
                                            }
                                        } else state = "STORE_INPUT"
                                    }
                                }
                                else -> state = "STORE_INPUT"
                            }
                        }
                    }
                }

                // 업소명 입력 다이얼로그
                "STORE_INPUT" -> StoreNameInputDialog(
                    onConfirm = { input ->
                        fetchBusiness(input) { list ->
                            if (list.isEmpty()) result = "업소를 찾지 못했습니다.\n업소명 입력으로 조회 기능을 확인하세요."
                            else { businessList = list; state = "LIST" }
                        }
                    },
                    onDismiss = { finish() }
                )

                // URL 입력 다이얼로그
                "URL_INPUT" -> StoreUrlInputDialog(
                    onConfirm = { url ->
                        lifecycleScope.launch {
                            val title = HtmlParser.extractTitle(url)
                            val sanitized = title?.let { sanitizeStoreName(it) }
                            if (!sanitized.isNullOrEmpty()) {
                                fetchBusiness(sanitized) { list ->
                                    if (list.isEmpty()) result = "업소를 찾지 못했습니다.\n업소명 입력으로 조회 기능을 확인하세요."
                                    else { businessList = list; state = "LIST" }
                                }
                            } else {
                                result = "URL에서 업소명을 찾을 수 없습니다."
                                state = "RESULT"
                            }
                        }
                    },
                    onDismiss = { finish() }
                )

                // 업소 선택 리스트
                "LIST" -> BusinessListDialog(
                    businesses = businessList,
                    onSelect = { row ->
                        fetchViolations(row.LCNS_NO ?: "") { res ->
                            result = res
                            state = "RESULT"
                        }
                    },
                    onDismiss = { finish() }
                )

                // 최종 결과
                "RESULT" -> ViolationDialog(result = result, onClose = { finish() })
            }
        }
    }

    // ---------------- helpers ----------------

    private fun extractStoreName(i: Intent): String? {
        val quoteRe = Regex("""['"“”‘’](.+?)['"“”‘’]""")
        val bracketRe = Regex("""\[(.*?)]""")

        fun findInText(text: String): String? {
            quoteRe.find(text)?.groupValues?.get(1)?.let { return it.trim().replace("-", " ") }
            bracketRe.find(text)?.groupValues?.get(1)?.let { return it.trim().replace("-", " ") }
            return null
        }

        i.getStringExtra(Intent.EXTRA_TEXT)?.let { findInText(it) }?.let { return it }

        i.clipData?.let { clip ->
            for (idx in 0 until clip.itemCount) {
                clip.getItemAt(idx).text?.toString()?.let { findInText(it) }?.let { return it }
            }
        }
        return null
    }

    private fun extractHttpUrl(i: Intent): String? {
        val re = Regex("""https?://[\w\-\._~:/?\#\[\]@!$&'()*+,;=%]+""")

        i.dataString?.let { re.find(it)?.value }?.let { return it }
        i.getStringExtra(Intent.EXTRA_TEXT)?.let { re.find(it)?.value }?.let { return it }

        i.clipData?.let { clip ->
            for (idx in 0 until clip.itemCount) {
                val item = clip.getItemAt(idx)
                item.text?.toString()?.let { re.find(it)?.value }?.let { return it }
                item.uri?.toString()?.let { re.find(it)?.value }?.let { return it }
                item.intent?.dataString?.let { re.find(it)?.value }?.let { return it }
            }
        }
        return null
    }

    private fun sanitizeStoreName(title: String): String {
        val bracketRe = Regex("""\[(.*?)]""")
        val raw = bracketRe.find(title)?.groupValues?.get(1)?.trim() ?: title.trim()
        return raw.replace("-", " ")
    }

    // ---------------- API ----------------

    private fun fetchBusiness(name: String, onResult: (List<BusinessRow>) -> Unit) {
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.getBusinessInfo(
                    apiKey = BuildConfig.API_KEY,
                    params = "BSSH_NM=$name&INDUTY_CD_NM=일반음식점"
                )
                onResult(res.body()?.I2500?.row ?: emptyList())
            } catch (e: Exception) {
                Log.e("API_ERROR", "조회 실패", e)
                onResult(emptyList())
            }
        }
    }

    private fun fetchViolations(licenseNo: String, onResult: (String) -> Unit) {
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.getViolations(
                    apiKey = BuildConfig.API_KEY,
                    params = "LCNS_NO=$licenseNo"
                )
                val match = res.body()?.I2630?.row?.firstOrNull()
                onResult(
                    if (match != null) """
                        업소명: ${match.PRCSCITYPOINT_BSSHNM}
                        업종: ${match.INDUTY_CD_NM}
                        주소: ${match.ADDR}
                        인허가번호: ${match.LCNS_NO}
                        확정일자: ${match.DSPS_DCSNDT}
                        위반내용: ${match.VILTCN}
                        처분유형: ${match.DSPS_TYPECD_NM}
                        기간: ${match.DSPS_BGNT} ~ ${match.DSPS_ENDDT}
                    """.trimIndent()
                    else "위반 이력이 없습니다."
                )
            } catch (e: Exception) {
                onResult("조회 실패: ${e.message}")
            }
        }
    }
}
