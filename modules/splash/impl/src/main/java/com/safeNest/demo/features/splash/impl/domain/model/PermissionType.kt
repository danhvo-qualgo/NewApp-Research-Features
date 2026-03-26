package com.safeNest.demo.features.splash.impl.domain.model

import android.Manifest
import com.safeNest.demo.features.splash.impl.R

enum class PermissionType(
    val nameRes: Int,
    val iconRes: Int,
    val descriptionRes: Int,
    val requestType: PermissionRequestType
) {
    ACCESSIBILITY(
        nameRes = R.string.permission_accessibility_title,
        iconRes = R.drawable.ic_face_content,
        descriptionRes = R.string.permission_accessibility_desc,
        requestType = PermissionRequestType.Settings
    ),
    NOTIFICATION_LISTENER(
        nameRes = R.string.permission_notification_listener_title,
        iconRes = R.drawable.ic_bell,
        descriptionRes = R.string.permission_notification_listener_desc,
        requestType = PermissionRequestType.Settings
    ),
    DISPLAY_OVER_APPS(
        nameRes = R.string.permission_display_over_apps_title,
        iconRes = R.drawable.ic_layers_two,
        descriptionRes = R.string.permission_display_over_apps_desc,
        requestType = PermissionRequestType.Settings
    ),
    PHONE_AND_CONTACTS(
        nameRes = R.string.permission_phone_contacts_title,
        iconRes = R.drawable.ic_phone_call,
        descriptionRes = R.string.permission_phone_contacts_desc,
        requestType = PermissionRequestType.RunTimes(listOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
        ))

    ),
    MICROPHONE(
        nameRes = R.string.permission_microphone_title,
        iconRes = R.drawable.ic_microphone,
        descriptionRes = R.string.permission_microphone_desc,
        requestType = PermissionRequestType.RunTime(Manifest.permission.RECORD_AUDIO)
    ),
    CALL_SCREENING(
        nameRes = R.string.permission_call_screening_title,
        iconRes = R.drawable.ic_shield,
        descriptionRes = R.string.permission_call_screening_desc,
        requestType = PermissionRequestType.Role
    ),
    CALL_REDIRECTION(
        nameRes = R.string.permission_call_redirection_title,
        iconRes = R.drawable.ic_phone_call,
        descriptionRes = R.string.permission_call_redirection_desc,
        requestType = PermissionRequestType.Role
    ),


//    PRIVATE_DNS(
//        nameRes = R.string.permission_private_dns_title,
//        iconRes = R.drawable.ic_server,
//        descriptionRes = R.string.permission_private_dns_desc,
//        requestType = PermissionRequestType.Settings
//    ),
//
//    DNS_PROXY(
//        nameRes = R.string.permission_dns_proxy_title,
//        iconRes = R.drawable.ic_server_02,
//        descriptionRes = R.string.permission_dns_proxy_desc,
//        requestType = PermissionRequestType.Settings
//    ),
//    SMS_FILTERING(
//        nameRes = R.string.permission_sms_filtering_title,
//        iconRes = R.drawable.ic_message_text_square,
//        descriptionRes = R.string.permission_sms_filtering_desc,
//        requestType = PermissionRequestType.Settings
//    ),
}