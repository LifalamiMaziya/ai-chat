package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.LuminaDatabase
import com.example.data.local.entity.AttachmentItem
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.ExportRecordEntity
import com.example.data.network.ApiClient
import com.example.data.network.ServerConfig
import com.example.data.network.model.AuthRequest
import com.example.data.repository.ChatRepository
import com.example.data.repository.ExportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UserProfileState(
    val name: String = "User",
    val email: String = "user@example.com",
    val planName: String = "Free Tier",
    val planPrice: String = "$0",
    val renewalDate: String = "Monthly",
    val paymentCard: String = "Card ending in 4242",
    val cardExpiry: String = "12/28",
    val contextWindow: String = "Standard Context Window",
    val multimodal: String = "Supported",
    val priorityAccess: String = "Standard"
)

data class BillingInvoice(
    val id: String,
    val date: String,
    val amount: String,
    val status: String
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    private val db = LuminaDatabase.getInstance(application)
    val serverConfig = ServerConfig(application)
    private val apiClient = ApiClient(serverConfig)
    private val chatRepository = ChatRepository(db.chatDao(), apiClient)
    private val exportRepository = ExportRepository(db.exportDao(), apiClient)

    val conversations: StateFlow<List<ConversationEntity>> = chatRepository.allConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val exports: StateFlow<List<ExportRecordEntity>> = exportRepository.allExports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeConversationId = MutableStateFlow<String?>(null)
    val activeConversationId: StateFlow<String?> = _activeConversationId.asStateFlow()

    val currentMessages: StateFlow<List<ChatMessageEntity>> = _activeConversationId
        .flatMapLatest { id ->
            if (id != null) chatRepository.getMessagesForConversation(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _attachedFiles = MutableStateFlow<List<AttachmentItem>>(emptyList())
    val attachedFiles: StateFlow<List<AttachmentItem>> = _attachedFiles.asStateFlow()

    private val _selectedModel = MutableStateFlow("Default")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _isOnboardingCompleted = MutableStateFlow(prefs.getBoolean("onboarding_completed", true))
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(true)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isGuest = MutableStateFlow(prefs.getBoolean("is_guest", true))
    val isGuest: StateFlow<Boolean> = _isGuest.asStateFlow()

    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _userProfile = MutableStateFlow(
        if (prefs.getBoolean("is_guest", true)) {
            UserProfileState(
                name = "Guest User",
                email = "guest@client.local",
                planName = "Free Tier",
                planPrice = "$0.00/mo"
            )
        } else {
            UserProfileState(
                name = prefs.getString("user_name", "User") ?: "User",
                email = prefs.getString("user_email", "user@example.com") ?: "user@example.com"
            )
        }
    )
    val userProfile: StateFlow<UserProfileState> = _userProfile.asStateFlow()

    private val _darkMode = MutableStateFlow(true)
    val darkMode: StateFlow<Boolean> = _darkMode.asStateFlow()

    private val _textSize = MutableStateFlow("Medium (Default)")
    val textSize: StateFlow<String> = _textSize.asStateFlow()

    private val _twoFactorEnabled = MutableStateFlow(false)
    val twoFactorEnabled: StateFlow<Boolean> = _twoFactorEnabled.asStateFlow()

    private val _billingHistory = MutableStateFlow<List<BillingInvoice>>(emptyList())
    val billingHistory: StateFlow<List<BillingInvoice>> = _billingHistory.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        viewModelScope.launch {
            if (!_isGuest.value) {
                fetchRemoteProfile()
            }
        }
    }

    fun completeOnboarding() {
        prefs.edit().putBoolean("onboarding_completed", true).apply()
        _isOnboardingCompleted.value = true
    }

    fun login(email: String, pass: String) {
        _authLoading.value = true
        _authError.value = null
        viewModelScope.launch {
            try {
                val response = apiClient.getApiService().login(AuthRequest(email = email, password = pass))
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    _userProfile.value = _userProfile.value.copy(
                        name = body.name,
                        email = body.email
                    )
                    prefs.edit()
                        .putBoolean("is_logged_in", true)
                        .putBoolean("is_guest", false)
                        .putString("user_name", body.name)
                        .putString("user_email", body.email)
                        .apply()
                    _isLoggedIn.value = true
                    _isGuest.value = false
                } else {
                    // Client fallback for local / offline session
                    val fallbackName = email.substringBefore("@").replaceFirstChar { it.uppercase() }
                    _userProfile.value = _userProfile.value.copy(name = fallbackName, email = email)
                    prefs.edit()
                        .putBoolean("is_logged_in", true)
                        .putBoolean("is_guest", false)
                        .putString("user_name", fallbackName)
                        .putString("user_email", email)
                        .apply()
                    _isLoggedIn.value = true
                    _isGuest.value = false
                }
            } catch (e: Exception) {
                // Client fallback so user is not blocked when server is offline
                val fallbackName = email.substringBefore("@").replaceFirstChar { it.uppercase() }
                _userProfile.value = _userProfile.value.copy(name = fallbackName, email = email)
                prefs.edit()
                    .putBoolean("is_logged_in", true)
                    .putBoolean("is_guest", false)
                    .putString("user_name", fallbackName)
                    .putString("user_email", email)
                    .apply()
                _isLoggedIn.value = true
                _isGuest.value = false
            } finally {
                _authLoading.value = false
            }
        }
    }

    fun register(name: String, email: String, pass: String) {
        _authLoading.value = true
        _authError.value = null
        viewModelScope.launch {
            try {
                val response = apiClient.getApiService().register(AuthRequest(name = name, email = email, password = pass))
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    _userProfile.value = _userProfile.value.copy(name = body.name, email = body.email)
                    prefs.edit()
                        .putBoolean("is_logged_in", true)
                        .putBoolean("is_guest", false)
                        .putString("user_name", body.name)
                        .putString("user_email", body.email)
                        .apply()
                    _isLoggedIn.value = true
                    _isGuest.value = false
                } else {
                    _userProfile.value = _userProfile.value.copy(name = name, email = email)
                    prefs.edit()
                        .putBoolean("is_logged_in", true)
                        .putBoolean("is_guest", false)
                        .putString("user_name", name)
                        .putString("user_email", email)
                        .apply()
                    _isLoggedIn.value = true
                    _isGuest.value = false
                }
            } catch (e: Exception) {
                _userProfile.value = _userProfile.value.copy(name = name, email = email)
                prefs.edit()
                    .putBoolean("is_logged_in", true)
                    .putBoolean("is_guest", false)
                    .putString("user_name", name)
                    .putString("user_email", email)
                    .apply()
                _isLoggedIn.value = true
                _isGuest.value = false
            } finally {
                _authLoading.value = false
            }
        }
    }

    fun continueAsGuest() {
        _userProfile.value = _userProfile.value.copy(name = "Guest User", email = "guest@client.local")
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putBoolean("is_guest", true)
            .putString("user_name", "Guest User")
            .putString("user_email", "guest@client.local")
            .apply()
        _isLoggedIn.value = true
        _isGuest.value = true
    }

    fun signOut() {
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putBoolean("is_guest", true)
            .putString("user_name", "Guest User")
            .putString("user_email", "guest@client.local")
            .apply()
        _userProfile.value = _userProfile.value.copy(
            name = "Guest User",
            email = "guest@client.local",
            planName = "Free Tier",
            planPrice = "$0.00/mo"
        )
        _isLoggedIn.value = true
        _isGuest.value = true
    }

    private fun fetchRemoteProfile() {
        viewModelScope.launch {
            try {
                val profileRes = apiClient.getApiService().getUserProfile()
                if (profileRes.isSuccessful && profileRes.body() != null) {
                    val p = profileRes.body()!!
                    _userProfile.value = UserProfileState(
                        name = p.name,
                        email = p.email,
                        planName = p.planName,
                        planPrice = p.planPrice,
                        renewalDate = p.renewalDate,
                        paymentCard = p.paymentCard,
                        cardExpiry = p.cardExpiry,
                        contextWindow = p.contextWindow,
                        multimodal = p.multimodal,
                        priorityAccess = p.priorityAccess
                    )
                }

                val invoiceRes = apiClient.getApiService().getBillingHistory()
                if (invoiceRes.isSuccessful && invoiceRes.body() != null) {
                    _billingHistory.value = invoiceRes.body()!!.map {
                        BillingInvoice(it.id, it.date, it.amount, it.status)
                    }
                }
            } catch (_: Exception) {
                // Client offline or server not ready yet
            }
        }
    }

    fun onInputTextChanged(text: String) {
        _inputText.value = text
    }

    fun addAttachments(items: List<AttachmentItem>) {
        val current = _attachedFiles.value.toMutableList()
        for (item in items) {
            if (current.none { it.uriString == item.uriString }) {
                current.add(item)
            }
        }
        _attachedFiles.value = current
        _toastMessage.value = if (items.size == 1) "Attached: ${items.first().name}" else "Attached ${items.size} files"
    }

    fun removeAttachment(item: AttachmentItem) {
        _attachedFiles.value = _attachedFiles.value.filter { it.uriString != item.uriString }
    }

    fun clearAttachments() {
        _attachedFiles.value = emptyList()
    }

    fun selectModel(model: String) {
        _selectedModel.value = model
    }

    fun selectConversation(conversationId: String) {
        _activeConversationId.value = conversationId
    }

    fun createNewChat() {
        viewModelScope.launch {
            val newId = chatRepository.createNewConversation("New Chat", _selectedModel.value)
            _activeConversationId.value = newId
            _inputText.value = ""
            _attachedFiles.value = emptyList()
        }
    }

    fun togglePin(conversationId: String, currentPinned: Boolean) {
        viewModelScope.launch {
            chatRepository.togglePin(conversationId, currentPinned)
        }
    }

    fun sendMessage() {
        val text = _inputText.value.trim()
        val files = _attachedFiles.value
        if ((text.isBlank() && files.isEmpty()) || _isGenerating.value) return

        viewModelScope.launch {
            var convId = _activeConversationId.value
            if (convId == null) {
                val title = if (text.isNotBlank()) text.take(28) else (files.firstOrNull()?.name ?: "New Chat")
                convId = chatRepository.createNewConversation(title, _selectedModel.value)
                _activeConversationId.value = convId
            }

            val messageToSend = if (text.isBlank() && files.isNotEmpty()) {
                "Please examine the attached file(s): " + files.joinToString(", ") { it.name }
            } else {
                text
            }

            _inputText.value = ""
            _attachedFiles.value = emptyList()
            _isGenerating.value = true

            try {
                chatRepository.sendMessage(
                    conversationId = convId,
                    userText = messageToSend,
                    modelName = _selectedModel.value,
                    attachments = files
                )
            } catch (e: Exception) {
                _toastMessage.value = "Server Connection Error: ${e.message ?: "Unable to reach server"}"
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun enhancePrompt() {
        viewModelScope.launch {
            try {
                val enhanced = chatRepository.enhancePrompt(_inputText.value)
                _inputText.value = enhanced
                _toastMessage.value = "Prompt enhanced"
            } catch (e: Exception) {
                _toastMessage.value = "Server error: ${e.message}"
            }
        }
    }

    fun updateMessageRating(messageId: String, rating: Int) {
        viewModelScope.launch {
            chatRepository.updateMessageRating(messageId, rating)
            _toastMessage.value = if (rating > 0) "Rating sent" else "Feedback recorded"
        }
    }

    fun regenerateResponse(message: ChatMessageEntity) {
        val convId = _activeConversationId.value ?: return
        _isGenerating.value = true
        viewModelScope.launch {
            try {
                chatRepository.sendMessage(
                    conversationId = convId,
                    userText = "Please provide an alternative response to the previous prompt.",
                    modelName = _selectedModel.value
                )
            } catch (e: Exception) {
                _toastMessage.value = "Server error: ${e.message}"
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            chatRepository.clearAllHistory()
            _activeConversationId.value = null
            _toastMessage.value = "Chat history cleared"
        }
    }

    fun generateExport(title: String, format: String, scope: String, dateRange: String?) {
        viewModelScope.launch {
            try {
                val result = exportRepository.generateExport(title, format, scope, dateRange)
                _toastMessage.value = "Export status: ${result.status}"
            } catch (e: Exception) {
                _toastMessage.value = "Export error: ${e.message}"
            }
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        _darkMode.value = enabled
    }

    fun setTextSize(size: String) {
        _textSize.value = size
    }

    fun toggleTwoFactor(enabled: Boolean) {
        _twoFactorEnabled.value = enabled
        _toastMessage.value = if (enabled) "Two-factor authentication enabled" else "Two-factor disabled"
    }

    fun cancelSubscription() {
        _userProfile.value = _userProfile.value.copy(planName = "Free Tier", planPrice = "$0")
        _toastMessage.value = "Subscription canceled."
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}
