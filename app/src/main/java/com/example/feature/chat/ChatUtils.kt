package com.example.feature.chat

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Small, pure formatting helpers for the Chat UI. These only format data that
 * already exists on ChatMessage / PrivateChatMeta (Firestore Timestamp) — no
 * new fields, no schema changes.
 */
object ChatUtils {

    fun messageTime(timestamp: Timestamp?): String {
        val date = timestamp?.toDate() ?: return ""
        return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date)
    }

    /** Compact relative time for conversation list rows: 2m, 3h, Yesterday, or a short date. */
    fun relativeTime(timestamp: Timestamp?): String {
        val date = timestamp?.toDate() ?: return ""
        val now = Calendar.getInstance()
        val then = Calendar.getInstance().apply { time = date }

        val diffMs = now.timeInMillis - then.timeInMillis
        val diffMinutes = diffMs / 60000
        val diffHours = diffMinutes / 60

        return when {
            diffMinutes < 1 -> "now"
            diffMinutes < 60 -> "${diffMinutes}m"
            isSameDay(now, then) -> "${diffHours}h"
            isYesterday(now, then) -> "Yesterday"
            diffHours < 24 * 7 -> SimpleDateFormat("EEE", Locale.getDefault()).format(date)
            else -> SimpleDateFormat("d MMM", Locale.getDefault()).format(date)
        }
    }

    /** Label used above a run of messages to separate days, e.g. Today / Yesterday / 12 June. */
    fun dateSeparatorLabel(timestamp: Timestamp?): String {
        val date = timestamp?.toDate() ?: return ""
        val now = Calendar.getInstance()
        val then = Calendar.getInstance().apply { time = date }
        return when {
            isSameDay(now, then) -> "Today"
            isYesterday(now, then) -> "Yesterday"
            else -> SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(date)
        }
    }

    /** Whether a date separator should be inserted before `current` given the previous message. */
    fun shouldShowDateSeparator(previous: ChatMessage?, current: ChatMessage): Boolean {
        val prevDate = previous?.timestamp?.toDate() ?: return true
        val currDate = current.timestamp?.toDate() ?: return false
        val prevCal = Calendar.getInstance().apply { time = prevDate }
        val currCal = Calendar.getInstance().apply { time = currDate }
        return !isSameDay(prevCal, currCal)
    }

    /** Whether consecutive messages from the same sender should be visually grouped (no repeated avatar/name). */
    fun shouldGroupWithPrevious(previous: ChatMessage?, current: ChatMessage): Boolean {
        if (previous == null) return false
        if (previous.senderId != current.senderId) return false
        val prevMs = previous.timestamp?.toDate()?.time ?: return false
        val currMs = current.timestamp?.toDate()?.time ?: return false
        return (currMs - prevMs) < 2 * 60 * 1000 // within 2 minutes
    }

    private fun isSameDay(a: Calendar, b: Calendar): Boolean =
        a.get(Calendar.YEAR) == b.get(Calendar.YEAR) && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)

    private fun isYesterday(now: Calendar, then: Calendar): Boolean {
        val yesterday = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
        return isSameDay(yesterday, then)
    }

    fun initials(name: String): String {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return "?"
        val parts = trimmed.split(" ").filter { it.isNotBlank() }
        return when {
            parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase()
            else -> trimmed.take(1).uppercase()
        }
    }
}
