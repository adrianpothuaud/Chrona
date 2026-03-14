package com.chrona.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.chrona.app.MainActivity
import com.chrona.app.data.model.TimerState
import com.chrona.app.data.model.TimerStatus
import com.chrona.app.timer.TimerService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimerNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_TIMER_ID = "chrona_timer"
        const val CHANNEL_ALARM_ID = "chrona_alarm"
        const val TIMER_NOTIFICATION_ID = 1001

        const val ACTION_PAUSE = "com.chrona.app.TIMER_PAUSE"
        const val ACTION_RESUME = "com.chrona.app.TIMER_RESUME"
        const val ACTION_SKIP = "com.chrona.app.TIMER_SKIP"
        const val ACTION_STOP = "com.chrona.app.TIMER_STOP"
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createChannels()
    }

    private fun createChannels() {
        val timerChannel = NotificationChannel(
            CHANNEL_TIMER_ID,
            "Timer",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Active timer notifications"
            setShowBadge(false)
        }
        val alarmChannel = NotificationChannel(
            CHANNEL_ALARM_ID,
            "Alarm",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alarm notifications"
        }
        notificationManager.createNotificationChannel(timerChannel)
        notificationManager.createNotificationChannel(alarmChannel)
    }

    fun buildTimerNotification(state: TimerState): android.app.Notification {
        val contentIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseOrResumeAction = if (state.status == TimerStatus.RUNNING) {
            NotificationCompat.Action(
                android.R.drawable.ic_media_pause,
                "Pause",
                buildActionPendingIntent(ACTION_PAUSE)
            )
        } else {
            NotificationCompat.Action(
                android.R.drawable.ic_media_play,
                "Resume",
                buildActionPendingIntent(ACTION_RESUME)
            )
        }

        val skipAction = NotificationCompat.Action(
            android.R.drawable.ic_media_next,
            "Skip",
            buildActionPendingIntent(ACTION_SKIP)
        )

        val stopAction = NotificationCompat.Action(
            android.R.drawable.ic_delete,
            "Stop",
            buildActionPendingIntent(ACTION_STOP)
        )

        val timeText = formatTime(state.remainingSeconds)
        val title = state.presetName.ifBlank { "Timer" }
        val statusText = when (state.status) {
            TimerStatus.RUNNING -> timeText
            TimerStatus.PAUSED -> "Paused • $timeText"
            TimerStatus.FINISHED -> "Finished!"
            TimerStatus.IDLE -> "Ready"
        }
        val roundText = if (state.totalRounds > 1) "Round ${state.currentRound}/${state.totalRounds}" else ""

        return NotificationCompat.Builder(context, CHANNEL_TIMER_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(if (roundText.isNotEmpty()) "$statusText  •  $roundText" else statusText)
            .setContentIntent(contentIntent)
            .setOngoing(state.status == TimerStatus.RUNNING || state.status == TimerStatus.PAUSED)
            .addAction(pauseOrResumeAction)
            .addAction(skipAction)
            .addAction(stopAction)
            .setSilent(true)
            .build()
    }

    fun notify(state: TimerState) {
        notificationManager.notify(TIMER_NOTIFICATION_ID, buildTimerNotification(state))
    }

    fun cancelTimerNotification() {
        notificationManager.cancel(TIMER_NOTIFICATION_ID)
    }

    private fun buildActionPendingIntent(action: String): PendingIntent =
        PendingIntent.getService(
            context,
            action.hashCode(),
            Intent(context, TimerService::class.java).also { it.action = action },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun formatTime(totalSeconds: Long): String {
        val h = totalSeconds / 3600
        val m = (totalSeconds % 3600) / 60
        val s = totalSeconds % 60
        return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
    }
}
