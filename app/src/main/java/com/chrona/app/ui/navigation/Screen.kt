package com.chrona.app.ui.navigation

sealed class Screen(val route: String) {
    // Main bottom-nav destinations
    data object Clock : Screen("clock")
    data object Alarm : Screen("alarm")
    data object Stopwatch : Screen("stopwatch")
    data object TimerList : Screen("timers")

    // Timer sub-screens
    data object TimerNew : Screen("timers/new")
    data class TimerEdit(val id: Long = 0L) : Screen("timers/{timerId}/edit") {
        companion object {
            const val ARG = "timerId"
            const val ROUTE = "timers/{timerId}/edit"
        }
        fun buildRoute() = "timers/$id/edit"
    }
    data class TimerDetail(val id: Long = 0L) : Screen("timers/{timerId}") {
        companion object {
            const val ARG = "timerId"
            const val ROUTE = "timers/{timerId}"
        }
        fun buildRoute() = "timers/$id"
    }
    data class TimerRunning(val id: Long = 0L) : Screen("timers/{timerId}/run") {
        companion object {
            const val ARG = "timerId"
            const val ROUTE = "timers/{timerId}/run"
        }
        fun buildRoute() = "timers/$id/run"
    }
}
