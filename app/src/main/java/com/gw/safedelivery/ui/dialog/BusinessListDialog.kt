package com.gw.safedelivery.ui.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gw.safedelivery.model.BusinessRow

@Composable
fun BusinessListDialog(
    businesses: List<BusinessRow>,
    onSelect: (BusinessRow) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("업소 선택") },
        text = {
            Column {
                businesses.forEach { business ->
                    Text(
                        text = "${business.BSSH_NM ?: "이름 없음"} (${business.ADDR ?: "주소 없음"})",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(business) }
                            .padding(8.dp)
                    )
                }
            }
        },
        confirmButton = {}
    )
}
