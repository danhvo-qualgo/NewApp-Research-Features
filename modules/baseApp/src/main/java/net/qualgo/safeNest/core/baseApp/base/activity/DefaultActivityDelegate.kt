package net.qualgo.safeNest.core.baseApp.base.activity

import android.app.Activity
import android.widget.Toast
//import com.uney.features.network.eventbus.NetworkingEventFlow
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@ActivityScoped
class DefaultActivityDelegate @Inject constructor(
    private val activity: Activity,
//    private val networkingEventFlow: NetworkingEventFlow,
) : ActivityDelegate {
    private val scope = MainScope()

    init {
        scope.launch {
//            networkingEventFlow.event.collect {
//                handleUnAuthorizedError()
//            }
        }
    }

    override fun handleUnAuthorizedError() {
        Toast.makeText(activity, "402 Error", Toast.LENGTH_SHORT).show()
    }

    override fun handleCommonError() {
        Toast.makeText(activity, "Common Error", Toast.LENGTH_SHORT).show()
    }
}