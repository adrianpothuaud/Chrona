package com.chrona.app.timer

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.chrona.app.data.model.TimerStatus
import com.chrona.app.notification.TimerNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

/**
 * Foreground service that keeps the timer alive when the app is backgrounded.
 * Communicates with [TimerEngine] which is singleton-scoped.
 */
@AndroidEntryPoint
class TimerService : Service() {

    @Inject lateinit var timerEngine: TimerEngine
    @Inject lateinit var notificationManager: TimerNotificationManager

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            TimerNotificationManager.ACTION_PAUSE -> timerEngine.pause()
            TimerNotificationManager.ACTION_RESUME -> timerEngine.resume()
            TimerNotificationManager.ACTION_SKIP -> timerEngine.skipRound()
            TimerNotificationManager.ACTION_STOP -> {
                timerEngine.stop()
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                // Initial start – promote to foreground
                val notification = notificationManager.buildTimerNotification(timerEngine.state.value)
                startForeground(TimerNotificationManager.TIMER_NOTIFICATION_ID, notification)
                observeTimer()
            }
        }
        return START_STICKY
    }

    private fun observeTimer() {
        scope.launch {
            timerEngine.state.collectLatest { state ->
                notificationManager.notify(state)
                if (state.status == TimerStatus.FINISHED || state.status == TimerStatus.IDLE) {
                    stopForeground(STOP_FOREGROUND_DETACH)
                    stopSelf()
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
