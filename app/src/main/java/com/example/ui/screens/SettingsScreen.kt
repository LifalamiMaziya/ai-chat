package com.example.ui.screens

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Error
import com.example.ui.theme.OnSurface
import com.example.ui.theme.OnSurfaceVariant
import com.example.ui.theme.Primary
import com.example.ui.theme.SecondaryContainer
import com.example.ui.theme.SurfaceBase
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceStroke
import com.example.ui.viewmodel.UserProfileState

@Composable
fun SettingsScreen(
    userProfile: UserProfileState,
    isLoggedIn: Boolean,
    isGuest: Boolean = false,
    darkMode: Boolean,
    textSize: String,
    twoFactorEnabled: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    onSelectTextSize: (String) -> Unit,
    onToggleTwoFactor: (Boolean) -> Unit,
    onClearHistory: () -> Unit,
    onSignOut: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToExport: () -> Unit,
    onNavigateToSubscription: () -> Unit,
    onInvokeAuth: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showSessionsDialog by remember { mutableStateOf(false) }
    var showTextSizeDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

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
                    .testTag("settings_back_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = OnSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Settings",
                fontFamily = FontFamily.SansSerif,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = Primary,
                letterSpacing = (-0.5).sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // SECTION: Account
        SettingsSectionHeader(title = "ACCOUNT")

        if (isGuest) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, SurfaceStroke, RoundedCornerShape(14.dp))
                    .background(SurfaceElevated)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .border(1.dp, SurfaceStroke, CircleShape)
                            .background(SurfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "G",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }

                    Column {
                        Text(
                            text = "Guest Session",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        )
                        Text(
                            text = "Sign in to save and sync your conversations",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 12.5.sp,
                            color = OnSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = onInvokeAuth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("settings_guest_signin_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = SurfaceBase
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Login,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sign In / Register",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, SurfaceStroke, RoundedCornerShape(14.dp))
                    .background(SurfaceElevated)
            ) {
                // Profile Row
                val initials = if (userProfile.name.isNotBlank()) {
                    userProfile.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase()
                } else "U"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .border(1.dp, SurfaceStroke, CircleShape)
                            .background(SurfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryContainer
                        )
                    }

                    Column {
                        Text(
                            text = userProfile.name,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        )
                        Text(
                            text = userProfile.email,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = OnSurfaceVariant
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SurfaceStroke))

                // Subscription Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToSubscription() }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Subscription",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        )
                        Row(
                            modifier = Modifier.padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = userProfile.planName.uppercase(),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = Primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SurfaceStroke)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onNavigateToSubscription,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(SurfaceStroke)),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("manage_subscription_button")
                    ) {
                        Text("Manage", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: Data & Storage
        SettingsSectionHeader(title = "DATA & STORAGE")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, SurfaceStroke, RoundedCornerShape(14.dp))
                .background(SurfaceElevated)
        ) {
            SettingsNavigationRow(
                icon = Icons.Default.Delete,
                label = "Clear Chat History",
                onClick = { showClearHistoryDialog = true },
                isDestructive = true
            )
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SurfaceStroke))
            SettingsNavigationRow(
                icon = Icons.Default.Download,
                label = "Export Data",
                onClick = onNavigateToExport
            )
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SurfaceStroke))
            SettingsNavigationRow(
                icon = Icons.Default.Policy,
                label = "Privacy Policy",
                onClick = { showPrivacyDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: Security
        SettingsSectionHeader(title = "SECURITY")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, SurfaceStroke, RoundedCornerShape(14.dp))
                .background(SurfaceElevated)
        ) {
            SettingsNavigationRow(
                icon = Icons.Default.Key,
                label = "Change Password",
                onClick = { showPasswordDialog = true }
            )
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SurfaceStroke))
            SettingsNavigationRow(
                icon = Icons.Default.Shield,
                label = "Two-Factor Authentication",
                onClick = { onToggleTwoFactor(!twoFactorEnabled) },
                subtitle = if (twoFactorEnabled) "Enabled" else "Disabled"
            )
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SurfaceStroke))
            SettingsNavigationRow(
                icon = Icons.Default.Devices,
                label = "Active Sessions",
                onClick = { showSessionsDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: Appearance
        SettingsSectionHeader(title = "APPEARANCE")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, SurfaceStroke, RoundedCornerShape(14.dp))
                .background(SurfaceElevated)
        ) {
            // Dark Mode Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SurfaceContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DarkMode,
                            contentDescription = "Dark mode",
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Dark Mode",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Primary
                    )
                }

                Switch(
                    checked = darkMode,
                    onCheckedChange = onToggleDarkMode,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Primary,
                        checkedTrackColor = SecondaryContainer,
                        uncheckedThumbColor = OnSurfaceVariant,
                        uncheckedTrackColor = SurfaceStroke
                    )
                )
            }

            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SurfaceStroke))

            // Text Size
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showTextSizeDialog = true }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SurfaceContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TextFields,
                            contentDescription = "Text size",
                            tint = OnSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Text Size",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        )
                        Text(
                            text = textSize,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = OnSurfaceVariant
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = OnSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Sign Out Button
        if (isLoggedIn) {
            Spacer(modifier = Modifier.height(28.dp))
            OutlinedButton(
                onClick = { showSignOutDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = OnSurfaceVariant),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(SurfaceStroke)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("sign_out_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = if (isGuest) "Exit guest mode" else "Sign out",
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (isGuest) "Exit Guest Session" else "Sign Out",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    // Dialogs
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear Chat History?", color = Primary, fontWeight = FontWeight.Bold) },
            text = { Text("This will clear messages and threads on this device.", color = OnSurface) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearHistory()
                        showClearHistoryDialog = false
                    }
                ) {
                    Text("Clear", color = Error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel", color = OnSurfaceVariant)
                }
            },
            containerColor = SurfaceContainerHigh,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy Policy", color = Primary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Your conversations and uploaded data are sent to your configured backend server.",
                    color = OnSurface,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("Got it", color = SecondaryContainer)
                }
            },
            containerColor = SurfaceContainerHigh,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text("Change Password", color = Primary, fontWeight = FontWeight.Bold) },
            text = { Text("Password change requests are processed by your backend server.", color = OnSurface) },
            confirmButton = {
                TextButton(onClick = { showPasswordDialog = false }) {
                    Text("Done", color = SecondaryContainer)
                }
            },
            containerColor = SurfaceContainerHigh,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showSessionsDialog) {
        AlertDialog(
            onDismissRequest = { showSessionsDialog = false },
            title = { Text("Active Sessions", color = Primary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Current Session (This Device)", color = SecondaryContainer, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { showSessionsDialog = false }) {
                    Text("Close", color = Primary)
                }
            },
            containerColor = SurfaceContainerHigh,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showTextSizeDialog) {
        AlertDialog(
            onDismissRequest = { showTextSizeDialog = false },
            title = { Text("Select Text Size", color = Primary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Small", "Medium (Default)", "Large").forEach { sizeOption ->
                        TextButton(
                            onClick = {
                                onSelectTextSize(sizeOption)
                                showTextSizeDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(sizeOption, color = if (sizeOption == textSize) SecondaryContainer else OnSurface)
                        }
                    }
                }
            },
            confirmButton = {},
            containerColor = SurfaceContainerHigh,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text(if (isGuest) "Exit Guest Session?" else "Sign Out?", color = Primary, fontWeight = FontWeight.Bold) },
            text = { Text(if (isGuest) "You will return to the sign-in screen." else "Are you sure you want to sign out?", color = OnSurface) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSignOut()
                        showSignOutDialog = false
                    }
                ) {
                    Text(if (isGuest) "Exit" else "Sign Out", color = Error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel", color = OnSurfaceVariant)
                }
            },
            containerColor = SurfaceContainerHigh,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontFamily = FontFamily.Monospace,
        fontSize = 11.5.sp,
        fontWeight = FontWeight.Medium,
        color = OnSurfaceVariant,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsNavigationRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    subtitle: String? = null,
    isDestructive: Boolean = false,
    trailingIcon: ImageVector = Icons.Default.ChevronRight
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(SurfaceContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isDestructive) OnSurfaceVariant else Primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = label,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = OnSurfaceVariant
                    )
                }
            }
        }

        Icon(
            imageVector = trailingIcon,
            contentDescription = null,
            tint = OnSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}
