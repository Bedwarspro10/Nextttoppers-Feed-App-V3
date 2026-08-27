@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.feature.chat

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.CookieManager
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
const val CHAT_WEBSITE_URL = "https://mychatwebsite.com" // Change this to your actual Chat website domain
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
            isFetchingToken = false
            return
        }
        isFetchingToken = true
        authErrorState = null
        currentUser.getIdToken(false)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    idTokenState = task.result?.token
                    authErrorState = null
                } else {
                    authErrorState = task.exception?.localizedMessage ?: "Failed to retrieve Firebase ID Token"
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
                                
                                // Enable persistent Cookies for sessions
                                val cookieManager = CookieManager.getInstance()
                                cookieManager.setAcceptCookie(true)
                                cookieManager.setAcceptThirdPartyCookies(this, true)

                                webViewClient = object : WebViewClient() {
                                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                        super.onPageStarted(view, url, favicon)
                                        isPageLoading = true
                                        webErrorState = null
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        isPageLoading = false
                                    }

                                    override fun onReceivedError(
                                        view: WebView?,
                                        request: WebResourceRequest?,
                                        error: WebResourceError?
                                    ) {
                                        super.onReceivedError(view, request, error)
                                        // Ignore subresource errors, only trigger on main frame failures
                                        if (request?.isForMainFrame == true) {
                                            isPageLoading = false
                                            webErrorState = error?.description?.toString() ?: "Connection failed"
                                        }
                                    }

                                    override fun shouldOverrideUrlLoading(
                                        view: WebView?,
                                        request: WebResourceRequest?
                                    ): Boolean {
                                        val url = request?.url?.toString() ?: return false
                                        val parsedUri = Uri.parse(url)
                                        val allowedHost = Uri.parse(CHAT_WEBSITE_URL).host
                                        
                                        // Strict security restriction to allowed domain/host only!
                                        return if (parsedUri.host == allowedHost) {
                                            false // Load inside WebView
                                        } else {
                                            true // Block or drop external links
                                        }
                                    }
                                }
                                webViewInstance = this
                            }
                        },
                        update = { webView ->
                            if (!hasLoadedUrl) {
                                hasLoadedUrl = true
                                if (USE_POST_METHOD) {
                                    val postParams = "idToken=${URLEncoder.encode(idToken, "UTF-8")}"
                                    webView.postUrl(finalUrl, postParams.toByteArray(StandardCharsets.UTF_8))
                                } else {
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
