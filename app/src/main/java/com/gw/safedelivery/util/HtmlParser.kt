package com.gw.safedelivery.util

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup

object HtmlParser {
    private val client = OkHttpClient()

    // 여러 User-Agent 후보
    private val userAgents = listOf(
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120 Safari/537.36",
        "facebookexternalhit/1.1 (+http://www.facebook.com/externalhit_uatext)",
        "Twitterbot/1.0"
    )

    suspend fun extractTitle(url: String): String? = withContext(Dispatchers.IO) {
        for ((idx, ua) in userAgents.withIndex()) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", ua)
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.w("HtmlParser", "[$idx] UA 실패 (HTTP ${response.code}): $ua")
                        return@use
                    }

                    val body = response.body?.string() ?: return@use
                    val doc = Jsoup.parse(body)

                    val title = doc.selectFirst("meta[property=og:title]")?.attr("content")
                        ?.takeIf { it.isNotBlank() }
                        ?: doc.selectFirst("title")?.text()?.takeIf { it.isNotBlank() }
                        ?: doc.selectFirst("meta[name=twitter:title]")?.attr("content")

                    if (!title.isNullOrBlank()) {
                        Log.i("HtmlParser", "[$idx] UA 성공: $ua → $title")
                        return@withContext title
                    } else {
                        Log.w("HtmlParser", "[$idx] UA 시도했지만 title 없음: $ua")
                    }
                }
            } catch (e: Exception) {
                Log.e("HtmlParser", "[$idx] UA 예외 발생: $ua", e)
            }

            // 실패하면 다음 UA로 재시도
            Log.d("HtmlParser", "[$idx] UA 실패 → 다음 UA 시도")
        }

        Log.e("HtmlParser", "모든 UA 시도 실패")
        return@withContext null
    }
}
