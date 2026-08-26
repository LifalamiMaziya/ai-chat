package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AttachmentItem
import com.example.ui.theme.Error
import com.example.ui.theme.OnSurface
import com.example.ui.theme.OnSurfaceVariant
import com.example.ui.theme.Primary
import com.example.ui.theme.SecondaryContainer
import com.example.ui.theme.SurfaceBase
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceStroke

@Composable
fun ChatInputBar(
    inputText: String,
    onInputTextChanged: (String) -> Unit,
    attachedFiles: List<AttachmentItem>,
    onRemoveAttachment: (AttachmentItem) -> Unit,
    selectedModel: String,
    onModelSelected: (String) -> Unit,
    onSend: () -> Unit,
    onEnhancePrompt: () -> Unit,
    onAttachFile: () -> Unit,
    isGenerating: Boolean,
    modifier: Modifier = Modifier
) {
    val hasContent = inputText.trim().isNotEmpty() || attachedFiles.isNotEmpty()
    var isModelMenuExpanded by remember { mutableStateOf(false) }

    val availableModels = listOf(
        "Default",
        "Fast",
        "Precise"
    )

    Column(
        modifier = modifier
            .background(SurfaceBase)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Floating Card Container for the entire input
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = 1.dp,
                    color = if (hasContent) SurfaceStroke.copy(alpha = 0.9f) else SurfaceStroke,
                    shape = RoundedCornerShape(16.dp)
                )
                .background(SurfaceElevated)
        ) {
            // Attached files horizontal rail (if any attached)
            if (attachedFiles.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    attachedFiles.forEach { attachment ->
                        AttachedFilePill(
                            attachment = attachment,
                            onRemove = { onRemoveAttachment(attachment) }
                        )
                    }
                }
            }

            // Text Input Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                if (inputText.isEmpty() && attachedFiles.isEmpty()) {
                    Text(
                        text = "Message...",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 15.sp,
                            color = OnSurfaceVariant
                        )
                    )
                } else if (inputText.isEmpty() && attachedFiles.isNotEmpty()) {
                    Text(
                        text = "Add a message...",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 15.sp,
                            color = OnSurfaceVariant.copy(alpha = 0.7f)
                        )
                    )
                }

                BasicTextField(
                    value = inputText,
                    onValueChange = onInputTextChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 24.dp, max = 140.dp)
                        .testTag("chat_input_field"),
                    textStyle = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 16.sp,
                        color = OnSurface,
                        lineHeight = 22.sp
                    ),
                    cursorBrush = SolidColor(Primary)
                )
            }

            // Toolbar row (Attachments, Model Selector, Send Button)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceContainerLow.copy(alpha = 0.6f))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left tools: Attach, Model Selector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onAttachFile,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("attach_file_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Attach file",
                            tint = if (attachedFiles.isNotEmpty()) Primary else OnSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Model Selector Pill
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, SurfaceStroke, RoundedCornerShape(8.dp))
                                .background(SurfaceContainer)
                                .clickable { isModelMenuExpanded = true }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                .testTag("model_selector_pill"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = selectedModel,
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 12.sp,
                                color = OnSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = "Choose model",
                                tint = OnSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = isModelMenuExpanded,
                            onDismissRequest = { isModelMenuExpanded = false },
                            modifier = Modifier
                                .background(SurfaceContainerHigh)
                                .border(1.dp, SurfaceStroke, RoundedCornerShape(8.dp))
                        ) {
                            availableModels.forEach { model ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            if (model == selectedModel) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            } else {
                                                Spacer(modifier = Modifier.width(16.dp))
                                            }
                                            Text(
                                                text = model,
                                                color = if (model == selectedModel) Primary else OnSurfaceVariant,
                                                fontFamily = FontFamily.SansSerif,
                                                fontSize = 13.sp
                                            )
                                        }
                                    },
                                    onClick = {
                                        onModelSelected(model)
                                        isModelMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Send Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (hasContent) Primary else SurfaceContainer)
                        .clickable(enabled = hasContent && !isGenerating) { onSend() }
                        .padding(8.dp)
                        .testTag("send_message_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Send message",
                        tint = if (hasContent) SurfaceBase else OnSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AttachedFilePill(
    attachment: AttachmentItem,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceContainerHigh)
            .border(1.dp, SurfaceStroke, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val icon = when {
            attachment.isImage -> Icons.Default.Image
            attachment.name.endsWith(".kt") || attachment.name.endsWith(".py") ||
            attachment.name.endsWith(".js") || attachment.name.endsWith(".json") -> Icons.Default.Code
            else -> Icons.Default.Description
        }

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(14.dp)
        )

        Text(
            text = attachment.name,
            fontFamily = FontFamily.SansSerif,
            fontSize = 12.sp,
            color = Primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Remove attachment",
            tint = OnSurfaceVariant,
            modifier = Modifier
                .size(14.dp)
                .clickable { onRemove() }
        )
    }
}
