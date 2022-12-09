package org.yameida.worktool.utils

object RegexHelper {

    fun reverseRegexTitle(string: String): String {
        return string.replace("\\", "\\\\")
            .replace("*", "\\*")
            .replace("+", "\\+")
            .replace(".", "\\.")
            .replace("[", "\\[")
            .replace("]", "\\]")
            .replace("?", "\\?")
            .replace("^", "\\^")
            .replace("$", "\\$")
            .replace("{", "\\{")
            .replace("}", "\\}")
            .replace("|", "\\|")
            //企微自身存在限制
            .replace("-", "\\-")
            .replace("(", "\\(")
            .replace(")", "\\)")
    }

}