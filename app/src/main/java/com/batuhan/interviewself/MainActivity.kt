package com.batuhan.interviewself

import android.content.pm.ActivityInfo
import android.Manifest
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navOptions
import com.batuhan.interviewself.MainActivity.Companion.KEY_INTERVIEW_ID
import com.batuhan.interviewself.MainActivity.Companion.KEY_INTERVIEW_TYPE
import com.batuhan.interviewself.MainActivity.Companion.KEY_LANG_CODE
import com.batuhan.interviewself.presentation.container.ContainerScreen
import com.batuhan.interviewself.presentation.interview.create.CreateInterviewScreen
import com.batuhan.interviewself.presentation.interview.create.addstep.AddStepScreen
import com.batuhan.interviewself.presentation.interview.detail.InterviewDetailScreen
import com.batuhan.interviewself.presentation.interview.enter.InterviewScreen
import com.batuhan.interviewself.presentation.splash.SplashScreen
import com.batuhan.interviewself.ui.theme.InterviewselfTheme
import com.batuhan.interviewself.util.BrowserEvent
import com.batuhan.interviewself.util.isTablet
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    companion object {
        internal const val KEY_INTERVIEW_ID = "interview_id"
        internal const val KEY_INTERVIEW_TYPE = "interview_type"
        internal const val KEY_LANG_CODE = "lang_code"
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).toTypedArray()
    }

    private lateinit var customTabsIntent: CustomTabsIntent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
        if (isTablet()) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        setContent {
            InterviewselfTheme {
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
                    )
                }
            }
        }
    }

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.value == false)
                    permissionGranted = false
            }
            // todo
        }
}

@Composable
fun InterviewSelfApp(
    finishApplication: () -> Unit,
    sendBrowserEvent: (BrowserEvent) -> Unit,
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
            )
        }
        composable("create_interview") {
            CreateInterviewScreen(
                onBackPressed = { navController.popBackStack() },
                addStep = {
                    navController.navigate("add_step/$it")
                },
            )
        }

        composable(
            "add_step/{$KEY_INTERVIEW_ID}",
            arguments =
                listOf(
                    navArgument(KEY_INTERVIEW_ID) {
                        type = NavType.LongType
                    },
                ),
        ) {
            AddStepScreen(
                interviewId = it.arguments?.getLong(KEY_INTERVIEW_ID) ?: -1,
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
                    }
                ),
        ) {
            InterviewScreen(
                interviewId = it.arguments?.getLong(KEY_INTERVIEW_ID) ?: -1,
                interviewType = it.arguments?.getString(KEY_INTERVIEW_TYPE) ?: "",
                langCode = it.arguments?.getString(KEY_LANG_CODE) ?: "",
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }
    }
}
