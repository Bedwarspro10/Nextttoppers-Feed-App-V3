package com.example.feature.update

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoftwareUpdateScreen(
    viewModel: SoftwareUpdateViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var hasLoadedUrl by remember { mutableStateOf(false) }
    var isPageLoading by remember { mutableStateOf(true) }
    var webErrorState by remember { mutableStateOf<String?>(null) }

    // Intercept hardware Back press to navigate to previous app screen
    BackHandler {
        viewModel.cancelDownload()
        onNavigateBack()
    }

    // Direct event bridge collector to dispatch download status from native coroutines to web callbacks
    LaunchedEffect(webViewInstance) {
        val webView = webViewInstance ?: return@LaunchedEffect
        viewModel.downloadEvent.collect { event ->
            when (event) {
                is DownloadEvent.Started -> {
                    Log.d("SoftwareUpdateScreen", "Dispatching: onUpdateDownloadStarted")
                    webView.evaluateJavascript("if (typeof window.onUpdateDownloadStarted === 'function') { window.onUpdateDownloadStarted(); }", null)
                }
                is DownloadEvent.Progress -> {
                    Log.d("SoftwareUpdateScreen", "Dispatching: onUpdateDownloadProgress (${event.percent}%)")
                    webView.evaluateJavascript("if (typeof window.onUpdateDownloadProgress === 'function') { window.onUpdateDownloadProgress(${event.percent}); }", null)
                }
                is DownloadEvent.Completed -> {
                    Log.d("SoftwareUpdateScreen", "Dispatching: onUpdateDownloadCompleted")
                    webView.evaluateJavascript("if (typeof window.onUpdateDownloadCompleted === 'function') { window.onUpdateDownloadCompleted(); }", null)
                }
                is DownloadEvent.Failed -> {
                    Log.e("SoftwareUpdateScreen", "Dispatching: onUpdateDownloadFailed (${event.message})")
                    val escapedMsg = event.message.replace("'", "\\'")
                    webView.evaluateJavascript("if (typeof window.onUpdateDownloadFailed === 'function') { window.onUpdateDownloadFailed('$escapedMsg'); }", null)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Software update", 
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.cancelDownload()
                        onNavigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        webViewInstance?.reload()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert, 
                            contentDescription = "Reload",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            @SuppressLint("SetJavaScriptEnabled")
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = android.view.ViewGroup.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        // Optimal web settings
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.databaseEnabled = true
                        settings.cacheMode = WebSettings.LOAD_DEFAULT
                        settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

                        // Ensure cookies are properly enabled for the web session
                        val cookieManager = CookieManager.getInstance()
                        cookieManager.setAcceptCookie(true)
                        cookieManager.setAcceptThirdPartyCookies(this, true)

                        // Standard client setup with strict security checking
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
                                if (request?.isForMainFrame == true) {
                                    isPageLoading = false
                                    webErrorState = error?.description?.toString() ?: "Network error"
                                }
                            }

                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                val url = request?.url?.toString() ?: return false
                                val isAllowed = url.startsWith("https://nexttopper-feed.pages.dev/") || 
                                                url.startsWith("https://github.com/") ||
                                                url.startsWith("https://objects.githubusercontent.com/") ||
                                                url.startsWith("https://nexttopper-feed-chat-site.pages.dev/")

                                return if (isAllowed) {
                                    false // Let the WebView load the URL
                                } else {
                                    Log.w("SoftwareUpdateWebView", "Blocked navigation to unverified external URL: $url")
                                    true // Prevent navigation
                                }
                            }
                        }

                        // Pipe JavaScript console logs directly into Android Logcat for monitoring
                        webChromeClient = object : WebChromeClient() {
                            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                                consoleMessage?.let {
                                    val logMsg = "[Web JS Console] ${it.message()} (${it.sourceId()}:${it.lineNumber()})"
                                    if (it.messageLevel() == ConsoleMessage.MessageLevel.ERROR) {
                                        Log.e("SoftwareUpdateConsole", logMsg)
                                    } else {
                                        Log.d("SoftwareUpdateConsole", logMsg)
                                    }
                                }
                                return true
                            }
                        }

                        // Inject the requested JavaScript interface
                        addJavascriptInterface(
                            AndroidUpdateBridge(
                                context = ctx,
                                viewModel = viewModel,
                                onGoBack = {
                                    viewModel.cancelDownload()
                                    onNavigateBack()
                                }
                            ),
                            "AndroidUpdate"
                        )

                        webViewInstance = this
                    }
                },
                update = { webView ->
                    if (!hasLoadedUrl) {
                        hasLoadedUrl = true
                        webView.loadUrl("https://nexttopper-feed.pages.dev/update.html")
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Dynamic loading bar over the view
            if (isPageLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Fallback screen if network/resource load fails
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
                        text = "Unable to connect to Update Page",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMsg,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = {
                        webErrorState = null
                        hasLoadedUrl = false
                    }) {
                        Text("Retry Connection")
                    }
                }
            }
        }
    }
}

/**
 * Android JavaScript bridge as defined by specification
 */
class AndroidUpdateBridge(
    private val context: android.content.Context,
    private val viewModel: SoftwareUpdateViewModel,
    private val onGoBack: () -> Unit
) {
    @android.webkit.JavascriptInterface
    fun getAppVersion(): String {
        return viewModel.getAppVersion(context)
    }

    @android.webkit.JavascriptInterface
    fun getAppVersionCode(): Int {
        return viewModel.getAppVersionCode(context)
    }

    @android.webkit.JavascriptInterface
    fun downloadUpdate(url: String) {
        Log.d("AndroidUpdateBridge", "downloadUpdate requested for URL: $url")
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            viewModel.downloadUpdate(context, url)
        }
    }

    @android.webkit.JavascriptInterface
    fun goBack() {
        Log.d("AndroidUpdateBridge", "goBack triggered from JavaScript")
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            onGoBack()
        }
    }
}
