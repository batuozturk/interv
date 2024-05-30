package com.batuhan.interv.util

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.batuhan.interv.data.model.InterviewType

private object Route {
    const val CREATE_INTERVIEW = "create_interview"
    const val ENTER_INTERVIEW =
        "enter_interview/{${ArgParams.INTERVIEW_ID}}/{${ArgParams.INTERVIEW_TYPE}}/{${ArgParams.LANG_CODE}}"
    const val INTERVIEW_DETAIL = "interview_detail/{${ArgParams.INTERVIEW_ID}}"
    const val IMPORT_QUESTIONS = "import_questions"
    const val EXPORT_QUESTIONS = "export_questions"
    const val SPLASH = "splash"
    const val HOME = "home"
    const val ADD_STEP = "add_step/{${ArgParams.INTERVIEW_ID}}/{${ArgParams.LANG_CODE}}"
}

private object ArgParams {
    const val INTERVIEW_ID = "interviewId"
    const val INTERVIEW_TYPE = "interviewType"
    const val LANG_CODE = "langCode"

    fun toPath(param: String) = "{$param}"
}

sealed class Screen(val route: String, val navArguments: List<NamedNavArgument> = emptyList()) {
    object Splash : Screen(Route.SPLASH)

    object Home : Screen(Route.HOME)

    object CreateInterview : Screen(Route.CREATE_INTERVIEW)

    object ImportQuestions : Screen(Route.IMPORT_QUESTIONS)

    object ExportQuestions : Screen(Route.EXPORT_QUESTIONS)

    object InterviewDetail : Screen(
        Route.INTERVIEW_DETAIL,
        navArguments =
            listOf(
                navArgument(ArgParams.INTERVIEW_ID) {
                    type = NavType.StringType
                },
            ),
    ) {
        fun createRoute(interviewId: Long) =
            Route.INTERVIEW_DETAIL
                .replace(
                    ArgParams.toPath(ArgParams.INTERVIEW_ID),
                    interviewId.toString(),
                )
    }

    object EnterInterview : Screen(
        Route.ENTER_INTERVIEW,
        navArguments =
            listOf(
                navArgument(ArgParams.INTERVIEW_ID) {
                    type = NavType.StringType
                },
                navArgument(ArgParams.INTERVIEW_TYPE) {
                    type = NavType.StringType
                },
                navArgument(ArgParams.LANG_CODE) {
                    type = NavType.StringType
                },
            ),
    ) {
        fun createRoute(
            interviewId: Long,
            interviewType: InterviewType,
            langCode: String,
        ) = Route.ENTER_INTERVIEW
            .replace(
                ArgParams.toPath(ArgParams.INTERVIEW_ID),
                interviewId.toString(),
            ).replace(ArgParams.toPath(ArgParams.INTERVIEW_TYPE), interviewType.name)
            .replace(ArgParams.toPath(ArgParams.LANG_CODE), langCode)
    }

    object AddStep : Screen(
        Route.ADD_STEP,
        navArguments =
        listOf(
            navArgument(ArgParams.INTERVIEW_ID) {
                type = NavType.StringType
            },
            navArgument(ArgParams.LANG_CODE) {
                type = NavType.StringType
            },
        ),
    ) {
        fun createRoute(
            interviewId: Long,
            langCode: String,
        ) = Route.ADD_STEP
            .replace(
                ArgParams.toPath(ArgParams.INTERVIEW_ID),
                interviewId.toString(),
            ).replace(ArgParams.toPath(ArgParams.LANG_CODE), langCode)
    }
}
