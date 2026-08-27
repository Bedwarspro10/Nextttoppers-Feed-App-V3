@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.feature.chat

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.firebase.auth.FirebaseAuth
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * CONFIGURATION CONSTANTS FOR SECURE FIREBASE CHAT WEBVIEW HANDOFF
 */
const val CHAT_WEBSITE_URL = "https://nexttopper-feed-chat-site.pages.dev" // Change this to your actual Chat website domain
const val USE_POST_METHOD = false // Set to true to use HTTP POST, false to use URL hash fragment handoff

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onCloseWebView: () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    // Authentication Handoff States
    var idTokenState by remember { mutableStateOf<String?>(null) }
    var authErrorState by remember { mutableStateOf<String?>(null) }
    var isFetchingToken by remember { mutableStateOf(true) }

    // WebView States
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var isPageLoading by remember { mutableStateOf(false) }
    var webErrorState by remember { mutableStateOf<String?>(null) }
    var hasLoadedUrl by remember { mutableStateOf(false) }

    // Helper function to fetch the Firebase ID token securely
    fun fetchIdToken() {
        if (currentUser == null) {
            Log.e("ChatWebView", "currentUser is NULL. Cannot fetch token.")
            isFetchingToken = false
            return
        }
        Log.d("ChatWebView", "currentUser exists: uid=${currentUser.uid}")
        isFetchingToken = true
        authErrorState = null
        
        // Force token refresh (true) to guarantee that the website receives a 100% valid, unexpired ID token
        currentUser.getIdToken(true)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result?.token
                    if (!token.isNullOrEmpty()) {
                        Log.d("ChatWebView", "getIdToken() succeeded! Token obtained successfully (length: ${token.length})")
                        idTokenState = token
                        authErrorState = null
                    } else {
                        Log.e("ChatWebView", "getIdToken() returned an empty token.")
                        authErrorState = "Retrieved token is empty."
                    }
                } else {
                    val exceptionMsg = task.exception?.localizedMessage ?: "Unknown token retrieval error"
                    Log.e("ChatWebView", "getIdToken() failed: $exceptionMsg")
                    authErrorState = exceptionMsg
                }
                isFetchingToken = false
            }
    }

    // Initial token fetch
    LaunchedEffect(currentUser) {
        fetchIdToken()
    }

    // Set up Compose System Back Button Interceptor
    BackHandler(enabled = currentUser != null) {
        if (webViewInstance?.canGoBack() == true) {
            webViewInstance?.goBack()
        } else {
            onCloseWebView()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Community Chat",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onCloseWebView() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Home",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                // Scenario 1: User is not logged in to the Android App
                currentUser == null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Authentication Required",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Please log in to your account first before accessing the Community Chat.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Scenario 2: Active fetch of Firebase ID Token
                isFetchingToken -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Securing chat authentication session...",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Scenario 3: Token fetch failed (Auth Error)
                authErrorState != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Authentication Handoff Failed",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = authErrorState ?: "Could not authenticate with server.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { fetchIdToken() }) {
                            Text("Retry Authentication")
                        }
                    }
                }

                // Scenario 4: Successful verification! Load secure WebView container
                idTokenState != null -> {
                    val idToken = idTokenState!!
                    
                    // Formulate URL depending on selected handoff mode
                    val finalUrl = remember(idToken) {
                        if (USE_POST_METHOD) {
                            "$CHAT_WEBSITE_URL/auth-handoff"
                        } else {
                            "$CHAT_WEBSITE_URL/auth-handoff#idToken=${URLEncoder.encode(idToken, "UTF-8")}"
                        }
                    }

                    // WebView Component
                    @SuppressLint("SetJavaScriptEnabled")
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                layoutParams = android.view.ViewGroup.LayoutParams(
                                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                
                                // Secure and optimal WebView configurations
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.databaseEnabled = true
                                settings.cacheMode = WebSettings.LOAD_DEFAULT
                                settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                                
                                // Enable persistent Cookies for sessions
                                val cookieManager = CookieManager.getInstance()
                                cookieManager.setAcceptCookie(true)
                                cookieManager.setAcceptThirdPartyCookies(this, true)

                                // Route all Javascript console logs directly to Android logcat for debugging
                                webChromeClient = object : WebChromeClient() {
                                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                                        consoleMessage?.let {
                                            val logMsg = "[JS Console] ${it.message()} at ${it.sourceId()}:${it.lineNumber()}"
                                            if (it.messageLevel() == ConsoleMessage.MessageLevel.ERROR) {
                                                Log.e("ChatWebViewConsole", logMsg)
                                            } else {
                                                Log.d("ChatWebViewConsole", logMsg)
                                            }
                                        }
                                        return true
                                    }
                                }

                                webViewClient = object : WebViewClient() {
                                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                        super.onPageStarted(view, url, favicon)
                                        val safeUrl = url?.substringBefore('#') ?: ""
                                        Log.d("ChatWebView", "onPageStarted: loading path: $safeUrl")
                                        isPageLoading = true
                                        webErrorState = null
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        val safeUrl = url?.substringBefore('#') ?: ""
                                        Log.d("ChatWebView", "onPageFinished: path loaded successfully: $safeUrl")
                                        isPageLoading = false
                                    }

                                    override fun onReceivedError(
                                        view: WebView?,
                                        request: WebResourceRequest?,
                                        error: WebResourceError?
                                    ) {
                                        super.onReceivedError(view, request, error)
                                        if (request?.isForMainFrame == true) {
                                            isPageLoading = false
                                            val errorMsg = error?.description?.toString() ?: "Connection failed"
                                            Log.e("ChatWebView", "onReceivedError on main frame: code=${error?.errorCode}, desc=$errorMsg, url=${request.url}")
                                            webErrorState = errorMsg
                                        }
                                    }

                                    override fun shouldOverrideUrlLoading(
                                        view: WebView?,
                                        request: WebResourceRequest?
                                    ): Boolean {
                                        val url = request?.url?.toString() ?: return false
                                        val parsedUri = Uri.parse(url)
                                        val host = parsedUri.host ?: ""
                                        val allowedHost = Uri.parse(CHAT_WEBSITE_URL).host ?: ""
                                        
                                        // Include allowed host, plus standard Firebase Authentication helper domains
                                        val isAllowed = host.endsWith(allowedHost) ||
                                                host.endsWith("firebaseapp.com") ||
                                                host.endsWith("googleapis.com") ||
                                                host.endsWith("google.com")
                                        
                                        Log.d("ChatWebView", "shouldOverrideUrlLoading: host=$host, allowedHost=$allowedHost, isAllowed=$isAllowed")
                                        return if (isAllowed) {
                                            false // Load inside the WebView
                                        } else {
                                            Log.w("ChatWebView", "shouldOverrideUrlLoading blocked external URL: $url")
                                            true // Prevent loading outside
                                        }
                                    }
                                }
                                webViewInstance = this
                            }
                        },
                        update = { webView ->
                            if (!hasLoadedUrl) {
                                hasLoadedUrl = true
                                val safeLogUrl = finalUrl.substringBefore('#')
                                if (USE_POST_METHOD) {
                                    Log.d("ChatWebView", "First load: posting token to $safeLogUrl")
                                    val postParams = "idToken=${URLEncoder.encode(idToken, "UTF-8")}"
                                    webView.postUrl(finalUrl, postParams.toByteArray(StandardCharsets.UTF_8))
                                } else {
                                    Log.d("ChatWebView", "First load: loading URL with hash token to $safeLogUrl")
                                    webView.loadUrl(finalUrl)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Overlay 1: Linear loading bar on top of the web view during transitions
                    if (isPageLoading) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Overlay 2: Beautiful error screen if WebView fails to load (offline / server issues)
                    webErrorState?.let { errorMsg ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Unable to connect to Chat",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Network offline or server is currently unreachable. ($errorMsg)",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(onClick = {
                                hasLoadedUrl = false
                                webErrorState = null
                                fetchIdToken()
                            }) {
                                Text("Try Again")
                            }
                        }
                    }
                }
            }
        }
    }
}
