package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ExportRecordEntity
import com.example.ui.theme.OnSurface
import com.example.ui.theme.OnSurfaceVariant
import com.example.ui.theme.Primary
import com.example.ui.theme.SecondaryContainer
import com.example.ui.theme.SurfaceBase
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.SurfaceContainerHighest
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceStroke

@Composable
fun ExportDataScreen(
    exports: List<ExportRecordEntity>,
    onGenerateExport: (title: String, format: String, scope: String, dateRange: String?) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var selectedScope by remember { mutableStateOf("ALL") } // "ALL" or "DATE_RANGE"
    var selectedFormat by remember { mutableStateOf("JSON") } // "JSON", "CSV", "PDF"
    var fromDate by remember { mutableStateOf("2026-08-01") }
    var toDate by remember { mutableStateOf("2026-08-25") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceBase)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("export_back_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = OnSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Export Data",
                fontFamily = FontFamily.SansSerif,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = Primary,
                letterSpacing = (-0.5).sp
            )
        }

        // Description
        Text(
            text = "Request a comprehensive archive of your interactions, settings, and generated content. Processing times may vary based on data volume.",
            fontFamily = FontFamily.SansSerif,
            fontSize = 14.5.sp,
            lineHeight = 22.sp,
            color = OnSurfaceVariant,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // SECTION 1: Selection Scope
        SettingsSectionHeader(title = "SELECTION SCOPE")

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Option 1: All Chats
            ScopeCard(
                icon = Icons.Default.Forum,
                title = "All Chats",
                description = "Complete history of all conversational threads and generated outputs.",
                isSelected = selectedScope == "ALL",
                onClick = { selectedScope = "ALL" }
            )

            // Option 2: Specific Date Range
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(
                        1.dp,
                        if (selectedScope == "DATE_RANGE") Primary else SurfaceStroke,
                        RoundedCornerShape(14.dp)
                    )
                    .background(SurfaceContainer)
                    .clickable { selectedScope = "DATE_RANGE" }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Specific Date Range",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        )
                    }

                    if (selectedScope == "DATE_RANGE") {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                    text = "Export data strictly within a defined chronological window.",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 13.sp,
                    color = OnSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                // Date Picker row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = fromDate,
                        onValueChange = { fromDate = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = Primary),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = SurfaceStroke,
                            focusedContainerColor = SurfaceContainerHigh,
                            unfocusedContainerColor = SurfaceContainerHigh
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Text(text = "to", fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = OnSurfaceVariant)

                    OutlinedTextField(
                        value = toDate,
                        onValueChange = { toDate = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = Primary),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = SurfaceStroke,
                            focusedContainerColor = SurfaceContainerHigh,
                            unfocusedContainerColor = SurfaceContainerHigh
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SECTION 2: Output Format
        SettingsSectionHeader(title = "OUTPUT FORMAT")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf(
                Triple("JSON", Icons.Default.DataObject, "JSON"),
                Triple("CSV", Icons.Default.TableChart, "CSV"),
                Triple("PDF", Icons.Default.PictureAsPdf, "PDF")
            ).forEach { (formatKey, icon, label) ->
                val isSelected = selectedFormat == formatKey
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .border(
                            1.dp,
                            if (isSelected) Primary else SurfaceStroke,
                            RoundedCornerShape(24.dp)
                        )
                        .background(if (isSelected) SurfaceContainerHighest else SurfaceContainer)
                        .clickable { selectedFormat = formatKey }
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                        .testTag("format_chip_$formatKey"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) Primary else OnSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = label,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected) Primary else OnSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SECTION 3: Privacy Note & Action Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, SurfaceStroke, RoundedCornerShape(14.dp))
                .background(SurfaceContainer)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = OnSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
                Column {
                    Text(
                        text = "Secure Delivery",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Primary
                    )
                    Text(
                        text = "A secure download link will be prepared from your local vault. The link expires after 24 hours to ensure strict privacy.",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = OnSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Button(
                onClick = {
                    val title = if (selectedScope == "ALL") "All Chats Archive" else "Custom Date Extract ($fromDate)"
                    val rangeText = if (selectedScope == "DATE_RANGE") "$fromDate to $toDate" else null
                    onGenerateExport(title, selectedFormat, selectedScope, rangeText)
                    Toast.makeText(context, "Export generation initiated...", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = SurfaceBase
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("generate_export_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Generate Export",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // SECTION 4: History / Recent Exports
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SurfaceStroke))
        Spacer(modifier = Modifier.height(20.dp))
        SettingsSectionHeader(title = "RECENT EXPORTS")

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            exports.forEach { item ->
                RecentExportItemRow(
                    export = item,
                    onDownload = {
                        val payload = item.filePayload ?: "Lumina Export Content [${item.title}]"
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, payload)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Export"))
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun ScopeCard(
    icon: ImageVector,
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                1.dp,
                if (isSelected) Primary else SurfaceStroke,
                RoundedCornerShape(14.dp)
            )
            .background(SurfaceContainer)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(24.dp)
            )
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = title,
            fontFamily = FontFamily.SansSerif,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            color = Primary
        )
        Text(
            text = description,
            fontFamily = FontFamily.SansSerif,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = OnSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun RecentExportItemRow(
    export: ExportRecordEntity,
    onDownload: () -> Unit
) {
    val isExpired = export.status == "Expired"
    val isProcessing = export.status == "Processing"

    val infiniteTransition = rememberInfiniteTransition(label = "spin")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spinAngle"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, SurfaceStroke, RoundedCornerShape(12.dp))
            .background(SurfaceContainer)
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SurfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isProcessing -> {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Processing",
                            tint = OnSurfaceVariant,
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(angle)
                        )
                    }
                    isExpired -> {
                        Icon(
                            imageVector = Icons.Default.TimerOff,
                            contentDescription = "Expired",
                            tint = OnSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Default.Archive,
                            contentDescription = "Archive",
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Column {
                Text(
                    text = export.title,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isExpired) OnSurfaceVariant else Primary,
                    textDecoration = if (isExpired) TextDecoration.LineThrough else TextDecoration.None
                )
                Text(
                    text = "Oct 24, 2026 • ${export.format} • ${export.fileSize}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.5.sp,
                    color = OnSurfaceVariant
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Status Chip
            val statusColor = when (export.status) {
                "Ready" -> SecondaryContainer
                "Processing" -> OnSurfaceVariant
                else -> OnSurfaceVariant.copy(alpha = 0.5f)
            }

            Text(
                text = export.status,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(statusColor.copy(alpha = 0.15f))
                    .border(1.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )

            if (export.status == "Ready") {
                IconButton(
                    onClick = onDownload,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("download_export_${export.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = "Download export",
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
