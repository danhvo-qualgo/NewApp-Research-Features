package com.safeNest.demo.features.phishingDetection.impl.presentation.models

data class WebsiteMetadata(
    val title: String,
    val description: String,
    val keywords: String,
    val ogTitle: String,
    val ogDescription: String,
    val bodyText: String
)