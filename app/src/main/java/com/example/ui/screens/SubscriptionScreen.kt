package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
import com.example.ui.theme.SurfaceBright
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceStroke
import com.example.ui.viewmodel.BillingInvoice
import com.example.ui.viewmodel.UserProfileState

@Composable
fun SubscriptionScreen(
    userProfile: UserProfileState,
    billingHistory: List<BillingInvoice>,
    onCancelSubscription: () -> Unit,
    onChangePlan: (String) -> Unit = {},
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showChangePlanDialog by remember { mutableStateOf(false) }
    var showEditCardDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var showAllInvoicesDialog by remember { mutableStateOf(false) }

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
                    .testTag("subscription_back_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = OnSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Subscription",
                fontFamily = FontFamily.SansSerif,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = Primary,
                letterSpacing = (-0.5).sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // SECTION 1: Active Plan Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, SurfaceStroke, RoundedCornerShape(16.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(SurfaceBright.copy(alpha = 0.35f), SurfaceElevated),
                        radius = 800f
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = userProfile.planName,
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Primary
                            )
                            Text(
                                text = "ACTIVE",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Primary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SurfaceStroke)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Row(
                            modifier = Modifier.padding(top = 8.dp),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = userProfile.planPrice,
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Primary
                            )
                            Text(
                                text = "/month",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 15.sp,
                                color = OnSurfaceVariant,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }

                        Text(
                            text = "Next renewal: ${userProfile.renewalDate}",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 14.sp,
                            color = OnSurfaceVariant,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { showChangePlanDialog = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceStroke)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("change_plan_button")
                ) {
                    Text(
                        text = "Change Plan",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SECTION 2: Plan Details
        SettingsSectionHeader(title = "PLAN DETAILS")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, SurfaceStroke, RoundedCornerShape(14.dp))
                .background(SurfaceElevated)
        ) {
            PlanFeatureRow(
                icon = Icons.Default.Memory,
                title = "128k Context Window",
                description = "Analyze vast documents and entire codebases in a single prompt."
            )
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SurfaceStroke))
            PlanFeatureRow(
                icon = Icons.Default.ImageSearch,
                title = "Multimodal Support",
                description = "Seamlessly process images, audio, and complex visual data."
            )
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SurfaceStroke))
            PlanFeatureRow(
                icon = Icons.Default.Bolt,
                title = "Priority Access",
                description = "Zero wait times during peak hours and access to latest models."
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SECTION 3: Payment Method
        SettingsSectionHeader(title = "PAYMENT METHOD")

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, SurfaceStroke, RoundedCornerShape(14.dp))
                .background(SurfaceElevated)
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
                        .size(44.dp, 32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(SurfaceBright)
                        .border(1.dp, SurfaceStroke, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = "Card",
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = userProfile.paymentCard,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Primary
                    )
                    Text(
                        text = userProfile.cardExpiry,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = OnSurfaceVariant
                    )
                }
            }

            TextButton(
                onClick = { showEditCardDialog = true },
                modifier = Modifier.testTag("edit_payment_button")
            ) {
                Text(
                    text = "Edit",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 14.sp,
                    color = SecondaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SECTION 4: Billing History
        SettingsSectionHeader(title = "BILLING HISTORY")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, SurfaceStroke, RoundedCornerShape(14.dp))
                .background(SurfaceElevated)
        ) {
            billingHistory.forEachIndexed { idx, invoice ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = invoice.date,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        )
                        Text(
                            text = "Invoice #${invoice.id}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.5.sp,
                            color = OnSurfaceVariant
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = invoice.amount,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            color = Primary
                        )
                        Text(
                            text = invoice.status.uppercase(),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.5.sp,
                            color = OnSurfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SurfaceStroke)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                        IconButton(
                            onClick = {
                                Toast.makeText(context, "Downloading receipt for ${invoice.id}", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download invoice",
                                tint = OnSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                if (idx < billingHistory.size - 1) {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SurfaceStroke))
                }
            }
        }

        TextButton(
            onClick = { showAllInvoicesDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            Text(
                text = "VIEW ALL INVOICES",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
                color = OnSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SECTION 5: Danger Zone / Cancel
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SurfaceStroke))
        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextButton(
                onClick = { showCancelDialog = true },
                modifier = Modifier.testTag("cancel_subscription_button")
            ) {
                Text(
                    text = "Cancel Subscription",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 15.sp,
                    color = OnSurfaceVariant
                )
            }
            Text(
                text = "Canceling will revert your account to the free tier at the end of your current billing cycle.",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.5.sp,
                color = OnSurfaceVariant.copy(alpha = 0.5f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier
                    .padding(start = 32.dp, end = 32.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))
    }

    // Dialogs
    if (showChangePlanDialog) {
        AlertDialog(
            onDismissRequest = { showChangePlanDialog = false },
            title = { Text("Select Subscription Tier", color = Primary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val currentPlan = userProfile.planName
                    PlanOptionCard(
                        "Free", "$0/mo",
                        "8k context, standard models",
                        isCurrent = currentPlan.equals("Free", ignoreCase = true)
                    ) {
                        showChangePlanDialog = false
                        onChangePlan("free")
                    }
                    PlanOptionCard(
                        "Pro", "$20/mo",
                        "128k context, multimodal, standard priority",
                        isCurrent = currentPlan.equals("Pro", ignoreCase = true)
                    ) {
                        showChangePlanDialog = false
                        onChangePlan("pro")
                    }
                    PlanOptionCard(
                        "Ultra", "$200/mo",
                        "1M+ context, multimodal, top priority access"
                        ,
                        isCurrent = currentPlan.equals("Ultra", ignoreCase = true)
                    ) {
                        showChangePlanDialog = false
                        onChangePlan("ultra")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showChangePlanDialog = false }) {
                    Text("Close", color = SecondaryContainer)
                }
            },
            containerColor = SurfaceContainerHigh,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showEditCardDialog) {
        AlertDialog(
            onDismissRequest = { showEditCardDialog = false },
            title = { Text("Update Payment Method", color = Primary, fontWeight = FontWeight.Bold) },
            text = { Text("Secure Stripe card update link will open in browser.", color = OnSurface) },
            confirmButton = {
                TextButton(onClick = { showEditCardDialog = false }) {
                    Text("OK", color = SecondaryContainer)
                }
            },
            containerColor = SurfaceContainerHigh,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Confirm Subscription Cancellation?", color = Primary, fontWeight = FontWeight.Bold) },
            text = { Text("You will retain Pro access until Nov 24, 2026. After that date, your account will revert to the Free tier.", color = OnSurface) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onCancelSubscription()
                        showCancelDialog = false
                    }
                ) {
                    Text("Confirm Cancellation", color = Error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Keep Pro Plan", color = OnSurfaceVariant)
                }
            },
            containerColor = SurfaceContainerHigh,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showAllInvoicesDialog) {
        AlertDialog(
            onDismissRequest = { showAllInvoicesDialog = false },
            title = { Text("All Historical Invoices", color = Primary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Oct 24, 2026 - $20.00 (INV-2039) [Paid]", fontFamily = FontFamily.Monospace, fontSize = 12.5.sp, color = SecondaryContainer)
                    Text("• Sep 24, 2026 - $20.00 (INV-1942) [Paid]", fontFamily = FontFamily.Monospace, fontSize = 12.5.sp, color = OnSurface)
                    Text("• Aug 24, 2026 - $20.00 (INV-1855) [Paid]", fontFamily = FontFamily.Monospace, fontSize = 12.5.sp, color = OnSurface)
                    Text("• Jul 24, 2026 - $20.00 (INV-1768) [Paid]", fontFamily = FontFamily.Monospace, fontSize = 12.5.sp, color = OnSurfaceVariant)
                    Text("• Jun 24, 2026 - $20.00 (INV-1681) [Paid]", fontFamily = FontFamily.Monospace, fontSize = 12.5.sp, color = OnSurfaceVariant)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAllInvoicesDialog = false }) {
                    Text("Close", color = Primary)
                }
            },
            containerColor = SurfaceContainerHigh,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun PlanFeatureRow(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SecondaryContainer,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(22.dp)
        )
        Column {
            Text(
                text = title,
                fontFamily = FontFamily.SansSerif,
                fontSize = 15.5.sp,
                fontWeight = FontWeight.Medium,
                color = Primary
            )
            Text(
                text = description,
                fontFamily = FontFamily.SansSerif,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = OnSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun PlanOptionCard(
    title: String,
    price: String,
    features: String,
    isCurrent: Boolean = false,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, if (isCurrent) SecondaryContainer else SurfaceStroke, RoundedCornerShape(10.dp))
            .background(SurfaceElevated)
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, color = Primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(price, color = if (isCurrent) SecondaryContainer else OnSurfaceVariant, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
        }
        Text(features, color = OnSurfaceVariant, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
    }
}
