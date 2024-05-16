package com.batuhan.interviewself

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navOptions
import com.batuhan.interviewself.MainActivity.Companion.KEY_INTERVIEW_ID
import com.batuhan.interviewself.MainActivity.Companion.KEY_INTERVIEW_TYPE
import com.batuhan.interviewself.MainActivity.Companion.KEY_LANGUAGE
import com.batuhan.interviewself.MainActivity.Companion.KEY_LANG_CODE
import com.batuhan.interviewself.presentation.container.ContainerScreen
import com.batuhan.interviewself.presentation.interview.create.CreateInterviewScreen
import com.batuhan.interviewself.presentation.interview.create.addstep.AddStepScreen
import com.batuhan.interviewself.presentation.interview.detail.InterviewDetailScreen
import com.batuhan.interviewself.presentation.interview.enter.InterviewScreen
import com.batuhan.interviewself.presentation.settings.SettingsType
import com.batuhan.interviewself.presentation.settings.exportquestions.ExportQuestionsScreen
import com.batuhan.interviewself.presentation.settings.importquestions.ImportQuestionsScreen
import com.batuhan.interviewself.presentation.splash.SplashScreen
import com.batuhan.interviewself.ui.theme.InterviewselfTheme
import com.batuhan.interviewself.util.BrowserEvent
import com.batuhan.interviewself.util.dataStore
import com.batuhan.interviewself.util.isTablet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    companion object {
        internal const val KEY_INTERVIEW_ID = "interview_id"
        internal const val KEY_INTERVIEW_TYPE = "interview_type"
        internal const val KEY_LANG_CODE = "lang_code"
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            ).toTypedArray()
        private val KEY_PREFERENCES_STYLE = booleanPreferencesKey("preferences_style")
        private val KEY_PREFERENCES_LANGUAGE = stringPreferencesKey("preferences_language")
        internal const val KEY_LANGUAGE = "language"
    }

    private lateinit var customTabsIntent: CustomTabsIntent

    private lateinit var langCode: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
        if (isTablet()) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        setContent {
            var darkTheme: Boolean? by remember {
                mutableStateOf(null)
            }
            LaunchedEffect(true) {
                dataStore.data.first().let {
                    darkTheme = it[KEY_PREFERENCES_STYLE] ?: run {
                        writeData(SettingsType.Style(false))
                        false
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
) {
    // todo navigation keys and argument keys
    val navController = rememberNavController()

    NavHost(navController, startDestination = "splash_screen") {
        composable("splash_screen") {
            SplashScreen(onBackPressed = finishApplication) {
                navController.navigate(
                    "main_screen",
                    navOptions =
                        navOptions {
                            popUpTo("splash_screen") {
                                inclusive = true
                            }
                        },
                )
            }
        }
        composable("main_screen") { // todo on back pressed test edilecek
            ContainerScreen(
                createInterview = {
                    navController.navigate(
                        "create_interview",
                    )
                },
                navigateToDetail = {
                    navController.navigate("interview_detail/$it")
                },
                enterInterview = { id, type, langCode ->
                    navController.navigate("enter_interview/$id/${type.name}/$langCode")
                },
                sendBrowserEvent = sendBrowserEvent,
                restartApplication = restartApplication,
                setStyle = setStyle,
                importQuestions = {
                    navController.navigate("import_questions")
                },
                exportQuestions = {
                    navController.navigate("export_questions")
                }
            )
        }
        composable("create_interview") {
            CreateInterviewScreen(
                onBackPressed = { navController.popBackStack() },
                addStep = { id, language ->
                    navController.navigate("add_step/$id/$language")
                },
            )
        }

        composable(
            "add_step/{$KEY_INTERVIEW_ID}/{$KEY_LANGUAGE}",
            arguments =
                listOf(
                    navArgument(KEY_INTERVIEW_ID) {
                        type = NavType.LongType
                    },
                    navArgument(KEY_LANGUAGE) {
                        type = NavType.StringType
                    },
                ),
        ) {
            AddStepScreen(
                interviewId = it.arguments?.getLong(KEY_INTERVIEW_ID) ?: -1,
                language = it.arguments?.getString(KEY_LANGUAGE) ?: "",
                onBackPressed = { navController.popBackStack() },
            )
        }

        composable(
            "interview_detail/{$KEY_INTERVIEW_ID}",
            arguments =
                listOf(
                    navArgument(KEY_INTERVIEW_ID) {
                        type = NavType.LongType
                    },
                ),
        ) {
            InterviewDetailScreen(
                interviewId = it.arguments?.getLong(KEY_INTERVIEW_ID) ?: -1,
                onBackPressed = { navController.popBackStack() },
                enterInterview = { id, type, langCode ->
                    navController.popBackStack()
                    navController.navigate("enter_interview/$id/${type.name}/$langCode")
                },
            )
        }

        composable(
            "enter_interview/{$KEY_INTERVIEW_ID}/{$KEY_INTERVIEW_TYPE}/{$KEY_LANG_CODE}",
            arguments =
                listOf(
                    navArgument(KEY_INTERVIEW_ID) {
                        type = NavType.LongType
                    },
                    navArgument(KEY_INTERVIEW_TYPE) {
                        type = NavType.StringType
                    },
                    navArgument(KEY_LANG_CODE) {
                        type = NavType.StringType
                    },
                ),
        ) {
            InterviewScreen(
                interviewId = it.arguments?.getLong(KEY_INTERVIEW_ID) ?: -1,
                interviewType = it.arguments?.getString(KEY_INTERVIEW_TYPE) ?: "",
                langCode = it.arguments?.getString(KEY_LANG_CODE) ?: "",
                onBackPressed = {
                    navController.popBackStack()
                },
            )
        }
        composable(
            "export_questions",
        ) {
            ExportQuestionsScreen(onBackPressed = { navController.popBackStack() })
        }
        composable(
            "import_questions",
        ) {
            ImportQuestionsScreen(onBackPressed = { navController.popBackStack() })
        }
    }
}
