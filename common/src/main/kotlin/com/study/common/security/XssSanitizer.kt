package com.study.common.security

object XssSanitizer {

    private val SCRIPT_PATTERN = Regex("<script[^>]*>.*?</script>", RegexOption.IGNORE_CASE)
    private val HTML_TAG_PATTERN = Regex("<[^>]+>")

    fun sanitize(input: String?): String? {
        if (input.isNullOrBlank()) return input
        var cleaned = input
        cleaned = SCRIPT_PATTERN.replace(cleaned, "")
        cleaned = cleaned
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
            .replace("/", "&#x2F;")
        return cleaned
    }

    fun stripHtml(input: String?): String? {
        if (input.isNullOrBlank()) return input
        return HTML_TAG_PATTERN.replace(input, "").trim()
    }
}
