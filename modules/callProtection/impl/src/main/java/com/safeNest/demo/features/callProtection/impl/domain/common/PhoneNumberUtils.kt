package com.safeNest.demo.features.callProtection.impl.domain.common
import android.telephony.PhoneNumberUtils

fun normalizePhoneNumber(phoneNumber: String, region: String = "VN"): String {
    val formattedNumber = PhoneNumberUtils.formatNumberToE164(phoneNumber, region)
    if (formattedNumber != null) {
        return formattedNumber
    }
    return phoneNumber.replace(Regex("[^0-9+]"), "")
}

fun formatBeautifulNumber(rawNumber: String, region: String = "VN"): String {
    val normalizePhoneNumber = normalizePhoneNumber(rawNumber, region)
    if (normalizePhoneNumber.startsWith("+") && normalizePhoneNumber.length == 12) {
        return normalizePhoneNumber.replaceFirst(Regex("(\\+\\d{2})(\\d{3})(\\d{3})(\\d{3})"), "$1 $2 $3 $4")
    }
    return normalizePhoneNumber
}