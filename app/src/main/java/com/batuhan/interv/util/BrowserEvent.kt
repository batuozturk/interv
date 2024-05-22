package com.batuhan.interv.util

sealed class BrowserEvent(val url: String) {

    object RateUs: BrowserEvent("https://play.google.com")
    object ContactUs: BrowserEvent("mailto:bozturk1999@gmail.com")
    object DownloadKonsol: BrowserEvent("https://play.google.com/store/apps/details?id=com.batuhan.konsol")
    object TermsOfService: BrowserEvent("https://play.google.com")
    object PrivacyPolicy: BrowserEvent("https://play.google.com")
}