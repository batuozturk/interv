package com.batuhan.interviewself.presentation.settings.importquestions

import android.content.ContentResolver
import android.net.Uri

interface ImportQuestionsEventHandler {

    fun importQuestions(contentResolver: ContentResolver, uri: Uri)
}