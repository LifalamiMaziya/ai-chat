package com.example

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.local.entity.AttachmentItem
import com.example.ui.components.NavigationDrawerContent
import com.example.ui.components.SearchDialog
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.ExportDataScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SubscriptionScreen
import com.example.ui.theme.LuminaNexusTheme
import com.example.ui.theme.SurfaceBase
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = viewModel()
            val darkMode by mainViewModel.darkMode.collectAsStateWithLifecycle()

            LuminaNexusTheme(darkTheme = darkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = SurfaceBase
                ) {
                    LuminaNexusApp(viewModel = mainViewModel)
                }
            }
        }
    }
}

@Composable
fun LuminaNexusApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // State collections
    val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val isGuest by viewModel.isGuest.collectAsStateWithLifecycle()
    val authLoading by viewModel.authLoading.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()

    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val activeConversationId by viewModel.activeConversationId.collectAsStateWithLifecycle()
    val currentMessages by viewModel.currentMessages.collectAsStateWithLifecycle()
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()
    val attachedFiles by viewModel.attachedFiles.collectAsStateWithLifecycle()
    val selectedModel by viewModel.selectedModel.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val darkMode by viewModel.darkMode.collectAsStateWithLifecycle()
    val textSize by viewModel.textSize.collectAsStateWithLifecycle()
    val twoFactorEnabled by viewModel.twoFactorEnabled.collectAsStateWithLifecycle()
    val exports by viewModel.exports.collectAsStateWithLifecycle()
    val billingHistory by viewModel.billingHistory.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    // Dialog state
    var showSearchDialog by remember { mutableStateOf(false) }

    // System File Attachment Picker Launcher
    val attachmentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val items = uris.map { uri ->
                AttachmentItem.fromUri(context, uri)
            }
            viewModel.addAttachments(items)
        }
    }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    // Determine start destination: starts immediately in chat screen as guest
    val startDestination = if (!isOnboardingCompleted) "onboarding" else "chat"

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        scrimColor = Color(0x99000000),
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.Transparent,
                drawerContentColor = Color.White
            ) {
                NavigationDrawerContent(
                    conversations = conversations,
                    activeConversationId = activeConversationId,
                    userProfile = userProfile,
                    isGuest = isGuest,
                    onSelectConversation = { id ->
                        viewModel.selectConversation(id)
                        coroutineScope.launch { drawerState.close() }
                    },
                    onNewChat = {
                        viewModel.createNewChat()
                        coroutineScope.launch { drawerState.close() }
                    },
                    onTogglePin = { id, currentPinned ->
                        viewModel.togglePin(id, currentPinned)
                    },
                    onSearchClick = {
                        showSearchDialog = true
                        coroutineScope.launch { drawerState.close() }
                    },
                    onSettingsClick = {
                        coroutineScope.launch { drawerState.close() }
                        navController.navigate("settings")
                    },
                    onInvokeAuth = {
                        coroutineScope.launch { drawerState.close() }
                        navController.navigate("auth")
                    }
                )
            }
        }
    ) {
        NavHost(navController = navController, startDestination = startDestination) {
            // Flow 1: Onboarding
            composable("onboarding") {
                OnboardingScreen(
                    onFinishOnboarding = {
                        viewModel.completeOnboarding()
                        navController.navigate("chat") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    }
                )
            }

            // Flow 2: Authentication
            composable("auth") {
                AuthScreen(
                    isLoading = authLoading,
                    errorMessage = authError,
                    canGoBack = true,
                    onNavigateBack = { navController.popBackStack() },
                    onLogin = { email, pass ->
                        viewModel.login(email, pass)
                        navController.navigate("chat") {
                            popUpTo("auth") { inclusive = true }
                        }
                    },
                    onRegister = { name, email, pass ->
                        viewModel.register(name, email, pass)
                        navController.navigate("chat") {
                            popUpTo("auth") { inclusive = true }
                        }
                    }
                )
            }

            // Flow 3: Main Chat Screen
            composable("chat") {
                ChatScreen(
                    messages = currentMessages,
                    inputText = inputText,
                    onInputTextChanged = { viewModel.onInputTextChanged(it) },
                    attachedFiles = attachedFiles,
                    onRemoveAttachment = { viewModel.removeAttachment(it) },
                    selectedModel = selectedModel,
                    onModelSelected = { viewModel.selectModel(it) },
                    onSendMessage = { viewModel.sendMessage() },
                    onEnhancePrompt = { viewModel.enhancePrompt() },
                    onAttachFile = {
                        try {
                            attachmentPickerLauncher.launch(arrayOf("*/*"))
                        } catch (e: Exception) {
                            Toast.makeText(context, "Could not open file picker: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onOpenDrawer = {
                        coroutineScope.launch { drawerState.open() }
                    },
                    onNewChat = { viewModel.createNewChat() },
                    onRateMessage = { id, rating -> viewModel.updateMessageRating(id, rating) },
                    onRegenerate = { msg -> viewModel.regenerateResponse(msg) },
                    isGenerating = isGenerating,
                    isGuest = isGuest,
                    onInvokeAuth = { navController.navigate("auth") }
                )
            }

            // Flow 4: Settings Screen
            composable("settings") {
                SettingsScreen(
                    userProfile = userProfile,
                    isLoggedIn = isLoggedIn,
                    isGuest = isGuest,
                    darkMode = darkMode,
                    textSize = textSize,
                    twoFactorEnabled = twoFactorEnabled,
                    onToggleDarkMode = { viewModel.toggleDarkMode(it) },
                    onSelectTextSize = { viewModel.setTextSize(it) },
                    onToggleTwoFactor = { viewModel.toggleTwoFactor(it) },
                    onClearHistory = { viewModel.clearAllHistory() },
                    onSignOut = {
                        viewModel.signOut()
                        navController.navigate("auth") {
                            popUpTo("chat") { inclusive = true }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToExport = { navController.navigate("export") },
                    onNavigateToSubscription = { navController.navigate("subscription") },
                    onInvokeAuth = { navController.navigate("auth") }
                )
            }

            // Flow 5: Export Data Screen
            composable("export") {
                ExportDataScreen(
                    exports = exports,
                    onGenerateExport = { title, format, scope, range ->
                        viewModel.generateExport(title, format, scope, range)
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Flow 6: Subscription Screen
            composable("subscription") {
                SubscriptionScreen(
                    userProfile = userProfile,
                    billingHistory = billingHistory,
                    onCancelSubscription = { viewModel.cancelSubscription() },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }

    // Interactive Dialogs
    if (showSearchDialog) {
        SearchDialog(
            conversations = conversations,
            onSelectConversation = { id ->
                viewModel.selectConversation(id)
            },
            onDismiss = { showSearchDialog = false }
        )
    }
}
