package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ConversationEntity
import com.example.ui.theme.OnSurface
import com.example.ui.theme.OnSurfaceVariant
import com.example.ui.theme.Primary
import com.example.ui.theme.SecondaryContainer
import com.example.ui.theme.Surface
import com.example.ui.theme.SurfaceBase
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceStroke
import com.example.ui.viewmodel.UserProfileState

@Composable
fun NavigationDrawerContent(
    conversations: List<ConversationEntity>,
    activeConversationId: String?,
    userProfile: UserProfileState,
    isGuest: Boolean = false,
    onSelectConversation: (String) -> Unit,
    onNewChat: () -> Unit,
    onTogglePin: (String, Boolean) -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onInvokeAuth: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val pinned = conversations.filter { it.isPinned }
    val recent = conversations.filter { !it.isPinned }
    var showAllRecent by remember { mutableStateOf(false) }
    val maxRecentVisible = 5
    val displayedRecent = if (showAllRecent || recent.size <= maxRecentVisible) recent else recent.take(maxRecentVisible)

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(320.dp)
            .background(Surface)
            .border(width = 1.dp, color = SurfaceStroke)
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Chats",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp,
                    color = Primary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = onSearchClick,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .testTag("drawer_search_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onNewChat,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceContainerHigh)
                        .border(1.dp, SurfaceStroke, RoundedCornerShape(10.dp))
                        .testTag("drawer_new_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Chat",
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Chats List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Pinned Section
            if (pinned.isNotEmpty()) {
                item {
                    Text(
                        text = "PINNED",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 11.sp,
                        color = OnSurfaceVariant.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }

                items(pinned, key = { it.id }) { conv ->
                    ConversationDrawerItem(
                        conversation = conv,
                        isSelected = conv.id == activeConversationId,
                        onClick = { onSelectConversation(conv.id) },
                        onTogglePin = { onTogglePin(conv.id, conv.isPinned) }
                    )
                }
            }

            // Recent Section
            item {
                Text(
                    text = "RECENT",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 11.sp,
                    color = OnSurfaceVariant.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 6.dp)
                )
            }

            if (recent.isEmpty() && pinned.isEmpty()) {
                item {
                    Text(
                        text = "No conversations yet",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 14.sp,
                        color = OnSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            items(displayedRecent, key = { it.id }) { conv ->
                ConversationDrawerItem(
                    conversation = conv,
                    isSelected = conv.id == activeConversationId,
                    onClick = { onSelectConversation(conv.id) },
                    onTogglePin = { onTogglePin(conv.id, conv.isPinned) }
                )
            }

            if (recent.size > maxRecentVisible) {
                item {
                    TextButton(
                        onClick = { showAllRecent = !showAllRecent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceElevated)
                            .border(1.dp, SurfaceStroke, RoundedCornerShape(8.dp))
                            .testTag("drawer_show_more_chats_button"),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Primary
                        )
                    ) {
                        Icon(
                            imageVector = if (showAllRecent) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (showAllRecent) "Show less" else "Show more (${recent.size - maxRecentVisible} more)",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        )
                    }
                }
            }
        }

        // Footer / Profile & Settings
        Spacer(modifier = Modifier.height(8.dp))
        val initials = if (isGuest) "G" else if (userProfile.name.isNotBlank()) {
            userProfile.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase()
        } else "U"

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 1.dp, color = SurfaceStroke, shape = RoundedCornerShape(12.dp))
                .background(SurfaceElevated)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .border(1.dp, SurfaceStroke, CircleShape)
                            .background(SurfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }

                    Column {
                        Text(
                            text = if (isGuest) "Guest Mode" else userProfile.name,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isGuest) "Temporary session" else userProfile.email,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 12.sp,
                            color = OnSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .testTag("drawer_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (isGuest) {
                Button(
                    onClick = onInvokeAuth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .testTag("drawer_invoke_auth_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = SurfaceBase
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Login,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Sign In / Register",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun ConversationDrawerItem(
    conversation: ConversationEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val relativeTime = formatRelativeTime(conversation.updatedAt)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (isSelected) {
                    Modifier
                        .background(SurfaceContainerHigh)
                        .border(1.dp, SurfaceStroke, RoundedCornerShape(12.dp))
                } else {
                    Modifier
                        .background(Color.Transparent)
                        .clickable { onClick() }
                }
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .clickable { onClick() }
            .testTag("conversation_item_${conversation.id}"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = conversation.title,
                fontFamily = FontFamily.SansSerif,
                fontSize = 14.5.sp,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                color = if (isSelected) Primary else OnSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = relativeTime,
                fontFamily = FontFamily.SansSerif,
                fontSize = 12.sp,
                color = OnSurfaceVariant.copy(alpha = 0.6f)
            )
        }

        IconButton(
            onClick = onTogglePin,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = if (conversation.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                contentDescription = if (conversation.isPinned) "Unpin" else "Pin",
                tint = if (conversation.isPinned) Primary else OnSurfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

fun formatRelativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val hours = diff / (1000 * 60 * 60)
    val days = hours / 24

    return when {
        hours < 1 -> "Just now"
        hours < 24 -> "${hours}h ago"
        days == 1L -> "Yesterday"
        days < 7 -> "${days}d ago"
        else -> "${days}d ago"
    }
}
