package com.gw.safedelivery

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gw.safedelivery.ui.theme.SafeDeliveryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SafeDeliveryTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 타이틀
        Text(
            text = "SafeDelivery",
            style = MaterialTheme.typography.headlineLarge
        )

        // 안내문구 카드
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Text(
                text = "배달 앱에서 표시되는 업소명과 상호로 등록된 업소명은 다를 수 있습니다.\n" +
                        "업소명 입력 시 '가게 정보' 또는 '매장 정보'를 확인하세요.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally),
                textAlign = TextAlign.Center
            )
        }

        // 버튼
        Button(
            onClick = {
                val intent = Intent(context, ShareReceiverActivity::class.java).apply {
                    putExtra("INPUT_MODE", "NAME")
                }
                context.startActivity(intent)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0D47A1)
            ),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text(
                text = "업소명 입력으로 조회",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
        }
    }
}
