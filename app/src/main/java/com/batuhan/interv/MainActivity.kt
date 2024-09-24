package com.batuhan.interv

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.batuhan.interv.MainActivity.Companion.KEY_INTERVIEW_ID
import com.batuhan.interv.MainActivity.Companion.KEY_INTERVIEW_TYPE
import com.batuhan.interv.MainActivity.Companion.KEY_LANG_CODE
import com.batuhan.interv.presentation.container.ContainerScreen
import com.batuhan.interv.presentation.interview.create.CreateInterviewScreen
import com.batuhan.interv.presentation.interview.create.addstep.AddStepScreen
import com.batuhan.interv.presentation.interview.detail.InterviewDetailScreen
import com.batuhan.interv.presentation.interview.enter.InterviewScreen
import com.batuhan.interv.presentation.settings.SettingsType
import com.batuhan.interv.presentation.settings.exportquestions.ExportQuestionsScreen
import com.batuhan.interv.presentation.settings.importquestions.ImportQuestionsScreen
import com.batuhan.interv.presentation.splash.SplashScreen
import com.batuhan.interv.ui.theme.InterviewselfTheme
import com.batuhan.interv.util.BrowserEvent
import com.batuhan.interv.util.Screen
import com.batuhan.interv.util.dataStore
import com.batuhan.interv.util.isTablet
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    companion object {
        internal const val KEY_INTERVIEW_ID = "interviewId"
        internal const val KEY_INTERVIEW_TYPE = "interviewType"
        internal const val KEY_LANG_CODE = "langCode"
        private val REQUIRED_PERMISSIONS =
            listOfNotNull(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.POST_NOTIFICATIONS else null,
            ).toTypedArray()
        private val REQUIRED_PERMISSIONS_INTERVIEW = listOfNotNull(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
        ).toTypedArray()
        private val KEY_PREFERENCES_STYLE = booleanPreferencesKey("preferences_style")
        private val KEY_PREFERENCES_LANGUAGE = stringPreferencesKey("preferences_language")
        internal val KEY_PREFERENCES_OPENAI_CLIENT_KEY =
            stringPreferencesKey("preferences_openai_key")
    }

    private lateinit var customTabsIntent: CustomTabsIntent

    private lateinit var langCode: String

    private lateinit var analytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        analytics = FirebaseAnalytics.getInstance(this)
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
        if (isTablet()) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        val appUpdate = intent.extras?.getString("app-update").toBoolean()
        setContent {
            var darkTheme: Boolean? by remember {
                mutableStateOf(null)
            }
            var apiToken: String? by remember {
                mutableStateOf(null)
            }
            LaunchedEffect(true) {
                dataStore.data.first().let {
                    darkTheme = it[KEY_PREFERENCES_STYLE] ?: run {
                        writeData(SettingsType.Style(true))
                        true
                    }
                    apiToken = it[KEY_PREFERENCES_OPENAI_CLIENT_KEY] ?: run {
                        writeData(SettingsType.ApiKey(""))
                        ""
                    }
                    if (it[KEY_PREFERENCES_LANGUAGE] == null) {
                        writeData(SettingsType.LangCode("en-US"))
                    }
                }
            }
            darkTheme?.let {
                InterviewselfTheme(it) {
                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        InterviewSelfApp(
                            finishApplication = { this.finish() },
                            sendBrowserEvent = {
                                customTabsIntent =
                                    CustomTabsIntent.Builder().setColorScheme(
                                        CustomTabsIntent.COLOR_SCHEME_SYSTEM,
                                    ).setBookmarksButtonEnabled(true).build()
                                customTabsIntent.launchUrl(this, Uri.parse(it.url))
                            },
                            restartApplication = {
                                val intent = Intent(this, MainActivity::class.java)
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                startActivity(intent)
                                finish()
                            },
                            setStyle = {
                                darkTheme = it
                            },
                            appUpdate = appUpdate,
                            onPermissionRequest = {
                                activityResultLauncher.launch(REQUIRED_PERMISSIONS_INTERVIEW)
                            }
                        )
                    }
                }
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val base: Context
        val config = newBase.resources.configuration
        runBlocking {
            langCode = newBase.dataStore.data.first()[KEY_PREFERENCES_LANGUAGE] ?: "en-US"
            val locales = LocaleList.forLanguageTags(langCode)
            config.setLocales(locales)
            base = newBase.createConfigurationContext(config)
            super.attachBaseContext(base)
        }
    }

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.value == false) {
                    permissionGranted = false
                } else if (it.value && it.key == Manifest.permission.POST_NOTIFICATIONS) {
                    Firebase.messaging.subscribeToTopic(getString(R.string.topic_subscribe))
                    Firebase.messaging.unsubscribeFromTopic(getString(R.string.topic_unsubscribe_1))
                    Firebase.messaging.unsubscribeFromTopic(getString(R.string.topic_unsubscribe_2))
                    Firebase.messaging.unsubscribeFromTopic(getString(R.string.topic_unsubscribe_3))
                    Firebase.messaging.unsubscribeFromTopic(getString(R.string.topic_unsubscribe_4))
                    Firebase.messaging.unsubscribeFromTopic(getString(R.string.topic_unsubscribe_5))
                    Firebase.messaging.unsubscribeFromTopic(getString(R.string.topic_unsubscribe_6))
                    Firebase.messaging.unsubscribeFromTopic(getString(R.string.topic_unsubscribe_7))
                    Firebase.messaging.unsubscribeFromTopic(getString(R.string.topic_unsubscribe_8))
                    Firebase.messaging.unsubscribeFromTopic(getString(R.string.topic_unsubscribe_9))
                }
            }
            // todo
        }

    fun writeData(settingsType: SettingsType) {
        lifecycleScope.launch {
            dataStore.edit { prefs ->
                when (settingsType) {
                    is SettingsType.Style -> prefs[KEY_PREFERENCES_STYLE] = settingsType.isDarkMode
                    is SettingsType.LangCode ->
                        prefs[KEY_PREFERENCES_LANGUAGE] =
                            settingsType.langCode

                    else -> {}
                }
            }
        }
    }
}

@Composable
fun InterviewSelfApp(
    finishApplication: () -> Unit,
    sendBrowserEvent: (BrowserEvent) -> Unit,
    restartApplication: () -> Unit,
    setStyle: (Boolean) -> Unit,
    appUpdate: Boolean,
    onPermissionRequest: () -> Unit,
) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(
                appUpdate,
                onBackPressed = finishApplication,
                navigateMainScreen = {
                    navController.navigate(
                        Screen.Home.route,
                        navOptions =
                            navOptions {
                                popUpTo(Screen.Splash.route) {
                                    inclusive = true
                                }
                            },
                    )
                },
                updateApp = {
                    sendBrowserEvent.invoke(BrowserEvent.UpdateApp)
                },
            )
        }
        composable(Screen.Home.route) { // todo on back pressed test edilecek
            ContainerScreen(
                createInterview = {
                    navController.navigate(
                        Screen.CreateInterview.route,
                    )
                },
                navigateToDetail = {
                    navController.navigate(Screen.InterviewDetail.createRoute(it))
                },
                enterInterview = { id, type, langCode, apiKey ->
                    navController.navigate(
                        Screen.EnterInterview.createRoute(
                            id,
                            type,
                            langCode,
                            apiKey,
                        ),
                    )
                },
                sendBrowserEvent = sendBrowserEvent,
                restartApplication = restartApplication,
                setStyle = setStyle,
                importQuestions = {
                    navController.navigate(Screen.ImportQuestions.route)
                },
                exportQuestions = {
                    navController.navigate(Screen.ExportQuestions.route)
                },
                onPermissionRequest = onPermissionRequest
            )
        }
        composable(Screen.CreateInterview.route) {
            CreateInterviewScreen(
                onBackPressed = { navController.popBackStack() },
                addStep = { id, language ->
                    navController.navigate(Screen.AddStep.createRoute(id, language))
                },
            )
        }

        composable(Screen.AddStep.route, Screen.AddStep.navArguments) {
            AddStepScreen(
                interviewId = it.arguments?.getString(KEY_INTERVIEW_ID)?.toLong() ?: -1,
                language = it.arguments?.getString(KEY_LANG_CODE) ?: "",
                onBackPressed = { navController.popBackStack() },
            )
        }

        composable(Screen.InterviewDetail.route, Screen.InterviewDetail.navArguments) {
            InterviewDetailScreen(
                interviewId = it.arguments?.getString(KEY_INTERVIEW_ID)?.toLong() ?: -1,
                onBackPressed = { navController.popBackStack() },
                enterInterview = { id, type, langCode, apiKey ->
                    navController.popBackStack()
                    navController.navigate(
                        Screen.EnterInterview.createRoute(
                            id,
                            type,
                            langCode,
                            apiKey,
                        ),
                    )
                },
            )
        }

        composable(
            Screen.EnterInterview.route,
            Screen.EnterInterview.navArguments,
        ) {
            InterviewScreen(
                interviewId = it.arguments?.getString(KEY_INTERVIEW_ID)?.toLong() ?: -1,
                interviewType = it.arguments?.getString(KEY_INTERVIEW_TYPE) ?: "",
                langCode = it.arguments?.getString(KEY_LANG_CODE) ?: "",
                onBackPressed = {
                    navController.popBackStack()
                },
            )
        }
        composable(
            Screen.ExportQuestions.route,
        ) {
            ExportQuestionsScreen(onBackPressed = { navController.popBackStack() })
        }
        composable(
            Screen.ImportQuestions.route,
        ) {
            ImportQuestionsScreen(onBackPressed = { navController.popBackStack() })
        }
    }
}
