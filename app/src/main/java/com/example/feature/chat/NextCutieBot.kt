package com.example.feature.chat

import java.util.Locale

enum class BotIntent {
    GREETING, HELP, CONFUSED, YOUTUBE, ANNOUNCEMENTS,
    CHAT_COMMUNITY, CHAT_PRIVATE, CONTACT, LOGIN, ADMIN, UNKNOWN
}

data class ParsedQuery(
    val intent: BotIntent,
    val subject: String?,
    val resource: String?,
    val chapter: String?
)

data class BotAction(
    val label: String,
    val route: String
)

data class BotMessage(
    val id: String,
    val fromBot: Boolean,
    val text: String,
    val actions: List<BotAction> = emptyList()
)

class NextCutieBot {

    private fun normalize(text: String): String = text.lowercase(Locale.ROOT).trim()

    private fun containsFuzzy(norm: String, phrases: List<String>): Boolean {
        return phrases.any { norm.contains(it) }
    }

    private fun parseQuery(text: String): ParsedQuery {
        val norm = normalize(text)
        
        var intent = BotIntent.UNKNOWN
        
        if (containsFuzzy(norm, listOf("hi", "hello", "hey", "hii", "namaste", "sup", "yo"))) {
            intent = BotIntent.GREETING
        } else if (containsFuzzy(norm, listOf("help confused", "what can you do", "features", "guide", "kya kar sakte"))) {
            intent = BotIntent.HELP
        } else if (containsFuzzy(norm, listOf("cannot find help", "kuch nahi mila", "samajh nahi", "nhi mil raha", "pata nahi"))) {
            intent = BotIntent.CONFUSED
        } else if (containsFuzzy(norm, listOf("youtube", "yt", "playlist", "channel", "video lectures"))) {
            intent = BotIntent.YOUTUBE
        } else if (containsFuzzy(norm, listOf("announcements", "notice", "update", "news", "suchna", "naya"))) {
            intent = BotIntent.ANNOUNCEMENTS
        } else if (containsFuzzy(norm, listOf("community chat", "group chat", "group", "sabse baat", "general chat"))) {
            intent = BotIntent.CHAT_COMMUNITY
        } else if (containsFuzzy(norm, listOf("private chat", "dm", "direct message", "kisi ko msg", "personal"))) {
            intent = BotIntent.CHAT_PRIVATE
        } else if (containsFuzzy(norm, listOf("contact", "message admin", "admin se baat", "support", "sir se baat"))) {
            intent = BotIntent.CONTACT
        } else if (containsFuzzy(norm, listOf("login", "signin", "register", "signup", "account"))) {
            intent = BotIntent.LOGIN
        } else if (containsFuzzy(norm, listOf("admin panel", "admin page", "manage"))) {
            intent = BotIntent.ADMIN
        }

        // Basic Subject Extraction
        val subject = when {
            containsFuzzy(norm, listOf("math", "maths", "ganit")) -> "maths"
            containsFuzzy(norm, listOf("science", "vigyan", "sci")) -> "science"
            containsFuzzy(norm, listOf("sst", "social", "history", "geo")) -> "sst"
            containsFuzzy(norm, listOf("english", "eng")) -> "english"
            containsFuzzy(norm, listOf("hindi")) -> "hindi"
            else -> null
        }

        // Basic Resource Extraction
        val resource = when {
            containsFuzzy(norm, listOf("notes", "pdf", "material")) -> "notes"
            containsFuzzy(norm, listOf("dpp", "practice", "assignment")) -> "dpp"
            containsFuzzy(norm, listOf("lecture", "video", "class")) -> "lecture"
            containsFuzzy(norm, listOf("test", "quiz", "mcq")) -> "test"
            else -> null
        }
        
        // Chapter extraction
        val chapterRegex = Regex("ch\\s*(\\d+)|chapter\\s*(\\d+)|part\\s*(\\d+)")
        val match = chapterRegex.find(norm)
        val chapter = match?.groupValues?.drop(1)?.firstOrNull { it.isNotEmpty() }

        return ParsedQuery(intent, subject, resource, chapter)
    }

    fun generateResponse(inputText: String): BotMessage {
        val parsed = parseQuery(inputText)
        val id = java.util.UUID.randomUUID().toString()

        if (parsed.subject != null && parsed.resource != null) {
            return BotMessage(
                id = id,
                fromBot = true,
                text = "Maine **${parsed.subject.replaceFirstChar { it.uppercase() }}** ke liye **${parsed.resource}** dhundh liya hai. Niche click karo:",
                actions = listOf(BotAction("View ${parsed.subject}", "courses/${parsed.subject}"))
            )
        }

        return when (parsed.intent) {
            BotIntent.GREETING -> BotMessage(
                id = id,
                fromBot = true,
                text = "Hey! Main **NextCutie-Feed** hoon. Kya chahiye? Notes, DPP, lecture, announcements — bas type karo!",
                actions = listOf(
                    BotAction("Maths", "courses/maths"),
                    BotAction("Science", "courses/science"),
                    BotAction("Updates", "announcements"),
                    BotAction("Chat", "chat")
                )
            )
            BotIntent.HELP -> BotMessage(
                id = id,
                fromBot = true,
                text = "Main **NextCutie-Feed** hoon — tumhara smart study bot!\n\nYeh kar sakta hoon:\n• Notes, DPP, lectures dhundhna\n• Subjects browse karna\n• Announcements check karna\n• Chat open karna\n• Admin se contact karna\n• YouTube lectures dikhana\n\nHinglish, Hindi, English — sab samajhta hoon!"
            )
            BotIntent.CONFUSED -> BotMessage(
                id = id,
                fromBot = true,
                text = "Agar kuch nahi mil raha, toh app ke search option ka use karo, ya seedhe Admin ko message karo. Kya main tumhe Contact page par le chaloon?",
                actions = listOf(BotAction("Contact Admin", "contact"))
            )
            BotIntent.YOUTUBE -> BotMessage(
                id = id,
                fromBot = true,
                text = "Aap hamare official YouTube videos YouTube tab mein dekh sakte hain.",
                actions = listOf(BotAction("Go to YouTube", "youtube"))
            )
            BotIntent.CONTACT -> BotMessage(
                id = id,
                fromBot = true,
                text = "Admin se baat karni hai? Contact form fill karo ya direct email bhejo.",
                actions = listOf(BotAction("Contact Admin", "contact"))
            )
            else -> BotMessage(
                id = id,
                fromBot = true,
                text = "Mujhe theek se samajh nahi aaya. 'Help' likh kar dekho kya options hain, ya koi subject/resource ka naam likho (e.g. 'Science notes').",
                actions = listOf(BotAction("Show Help", "bot_help"))
            )
        }
    }
}
