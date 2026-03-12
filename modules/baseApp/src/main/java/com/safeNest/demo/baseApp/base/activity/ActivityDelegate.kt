package com.safeNest.demo.baseApp.base.activity

interface ActivityDelegate {
    fun handleUnAuthorizedError()
    fun handleCommonError()
}