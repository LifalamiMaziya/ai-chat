package com.example.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.entity.AttachmentItem
import com.example.data.local.entity.ChatMessageEntity
import com.example.ui.components.ChatInputBar
import com.example.ui.components.CodeBlockView
import com.example.ui.theme.OnSurface
import com.example.ui.theme.OnSurfaceVariant
import com.example.ui.theme.Primary
import com.example.ui.theme.SurfaceBase
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceStroke
import com.example.ui.theme.SurfaceVariant

@Composable
fun ChatScreen(
    messages: List<ChatMessageEntity>,
    inputText: String,
    onInputTextChanged: (String) -> Unit,
    attachedFiles: List<AttachmentItem> = emptyList(),
    onRemoveAttachment: (AttachmentItem) -> Unit = {},
    selectedModel: String,
    onModelSelected: (String) -> Unit,
    onSendMessage: () -> Unit,
    onEnhancePrompt: () -> Unit,
    onAttachFile: () -> Unit,
    onOpenDrawer: () -> Unit,
    onNewChat: () -> Unit,
    onRateMessage: (String, Int) -> Unit,
    onRegenerate: (ChatMessageEntity) -> Unit,
    isGenerating: Boolean,
    isGuest: Boolean = false,
    onInvokeAuth: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceBase)
            .imePadding()
    ) {
        // Main Content Column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Chat Messages List or Empty State
            if (messages.isEmpty() && !isGenerating) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "How can I help you today?",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp,
                            color = Primary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Send a message or attach a document to get started.",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 14.sp,
                            color = OnSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(
                        top = 68.dp,
                        bottom = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        if (msg.sender == "user") {
                            UserMessageBubble(message = msg)
                        } else {
                            AiMessageBubble(
                                message = msg,
                                onRegenerate = onRegenerate
                            )
                        }
                    }

                    // Active generating indicator
                    if (isGenerating) {
                        item {
                            GeneratingIndicator()
                        }
                    }
                }
            }

            // Bottom Input Bar
            ChatInputBar(
                inputText = inputText,
                onInputTextChanged = onInputTextChanged,
                attachedFiles = attachedFiles,
                onRemoveAttachment = onRemoveAttachment,
                selectedModel = selectedModel,
                onModelSelected = onModelSelected,
                onSend = onSendMessage,
                onEnhancePrompt = onEnhancePrompt,
                onAttachFile = onAttachFile,
                isGenerating = isGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            )
        }

        // Floating Action Buttons Overlay (Completely transparent, purely floating buttons)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onOpenDrawer,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(SurfaceElevated)
                    .border(1.dp, SurfaceStroke, CircleShape)
                    .testTag("open_drawer_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open navigation drawer",
                    tint = Primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isGuest) {
                    Button(
                        onClick = onInvokeAuth,
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("top_sign_in_button"),
                        shape = RoundedCornerShape(19.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SurfaceElevated,
                            contentColor = Primary
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceStroke)),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Login,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = Primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Sign In",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        )
                    }
                }

                IconButton(
                    onClick = onNewChat,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SurfaceElevated)
                        .border(1.dp, SurfaceStroke, CircleShape)
                        .testTag("top_new_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Start new chat",
                        tint = Primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun UserMessageBubble(
    message: ChatMessageEntity,
    modifier: Modifier = Modifier
) {
    val attachments = remember(message.attachedFilesJson) {
        AttachmentItem.listFromJson(message.attachedFilesJson)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 48.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Attached files display
            if (attachments.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    attachments.forEach { att ->
                        if (att.isImage) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, SurfaceStroke, RoundedCornerShape(12.dp))
                                    .background(SurfaceElevated)
                            ) {
                                AsyncImage(
                                    model = Uri.parse(att.uriString),
                                    contentDescription = att.name,
                                    modifier = Modifier
                                        .size(160.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        } else {
                            val icon = when {
                                att.name.endsWith(".kt") || att.name.endsWith(".py") ||
                                att.name.endsWith(".js") || att.name.endsWith(".ts") ||
                                att.name.endsWith(".json") -> Icons.Default.Code
                                att.name.endsWith(".csv") || att.name.endsWith(".pdf") ||
                                att.name.endsWith(".txt") || att.name.endsWith(".doc") -> Icons.Default.Description
                                else -> Icons.Default.InsertDriveFile
                            }

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SurfaceElevated)
                                    .border(1.dp, SurfaceStroke, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(imageVector = icon, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                                Text(
                                    text = att.name,
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 13.sp,
                                    color = Primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            if (message.textContent.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                        .background(SurfaceVariant)
                        .border(1.dp, SurfaceStroke, RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = message.textContent,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 15.sp,
                        color = Primary,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AiMessageBubble(
    message: ChatMessageEntity,
    onRegenerate: (ChatMessageEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(end = 32.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                    .background(SurfaceElevated)
                    .border(1.dp, SurfaceStroke, RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (message.textContent.isNotBlank()) {
                        Text(
                            text = message.textContent,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 15.sp,
                            color = OnSurface,
                            lineHeight = 22.sp
                        )
                    }

                    if (!message.codeSnippet.isNullOrBlank()) {
                        CodeBlockView(
                            code = message.codeSnippet,
                            language = message.codeLanguage ?: "text"
                        )
                    }
                }
            }

            // Action row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 4.dp)
            ) {
                IconButton(
                    onClick = {
                        val textToCopy = buildString {
                            if (message.textContent.isNotBlank()) append(message.textContent)
                            if (!message.codeSnippet.isNullOrBlank()) {
                                if (isNotEmpty()) append("\n\n")
                                append(message.codeSnippet)
                            }
                        }
                        clipboardManager.setText(AnnotatedString(textToCopy))
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy message",
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                }

                IconButton(
                    onClick = { onRegenerate(message) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Regenerate response",
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun GeneratingIndicator(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceElevated)
                .border(1.dp, SurfaceStroke, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                strokeWidth = 2.dp,
                color = Primary
            )
            Text(
                text = "Thinking...",
                fontFamily = FontFamily.SansSerif,
                fontSize = 13.sp,
                color = OnSurfaceVariant
            )
        }
    }
}
