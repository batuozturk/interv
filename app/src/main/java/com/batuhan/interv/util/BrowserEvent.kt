package com.batuhan.interv.util

sealed class BrowserEvent(val url: String) {

    object RateUs: BrowserEvent("https://play.google.com/store/apps/details?id=com.batuhan.interv")
    object ContactUs: BrowserEvent("mailto:batuoztrk99@gmail.com")
    object DownloadKonsol: BrowserEvent("https://play.google.com/store/apps/details?id=com.batuhan.konsol")
    object PrivacyPolicy: BrowserEvent("https://sites.google.com/view/interv-privacy-policy/")
}