package com.batuhan.interviewself.presentation.settings.exportquestions

import android.content.ContentResolver
import android.net.Uri

interface ExportQuestionsEventHandler {

    fun exportQuestions(contentResolver: ContentResolver, uri: Uri, isSharing: Boolean)

    fun updateSelectedLanguage(langCode: String)

    fun shareQuestions(success: Boolean)
}