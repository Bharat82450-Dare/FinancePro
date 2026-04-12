package com.financeapp.utils

object UpiSmsParser {

    data class ParsedTransaction(
        val amount: Double,
        val type: String,       // "EXPENSE" (debit) or "INCOME" (credit)
        val merchant: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    // Known bank/UPI SMS sender IDs (partial matches, case-insensitive)
    private val TRUSTED_SENDERS = listOf(
        "HDFCBK", "SBIUPI", "SBISMS", "ICICIB", "AXISBK", "KOTAKB",
        "PAYTM", "PHONEPE", "GPAY", "BHIMUPI", "YESBNK", "IDFCBK",
        "BOIUPI", "PNBSMS", "INDBNK", "CANBNK", "UNIONB", "CENTBK",
        "BOBSMS", "IOBANK", "SYNDBK", "LVBANK", "JKBANK", "FEDBNK"
    )

    // Regex to extract rupee amount
    private val AMOUNT_REGEX = Regex(
        """(?:INR|Rs\.?|₹)\s*([\d,]+(?:\.\d{1,2})?)""",
        RegexOption.IGNORE_CASE
    )

    // Regex to extract VPA (UPI ID like name@bank)
    private val VPA_REGEX = Regex(
        """(?:to|from)\s+(?:VPA\s+)?([a-zA-Z0-9._-]+@[a-zA-Z0-9]+)""",
        RegexOption.IGNORE_CASE
    )

    // Regex to extract merchant name from "paid to X via" or "sent to X on"
    private val MERCHANT_REGEX = Regex(
        """(?:paid\s+to|sent\s+to|at)\s+([A-Za-z0-9 &.'_-]{3,30})(?:\s+(?:via|on|using|ref|upi|\.)|\.|$)""",
        RegexOption.IGNORE_CASE
    )

    fun parse(sender: String, body: String): ParsedTransaction? {
        // Filter: only process messages from trusted bank/UPI senders
        val senderUpper = sender.uppercase().replace(Regex("[^A-Z0-9]"), "")
        val isTrusted = TRUSTED_SENDERS.any { senderUpper.contains(it) }
        val hasUpiKeyword = body.contains("UPI", ignoreCase = true) ||
                body.contains("IMPS", ignoreCase = true) ||
                body.contains("NEFT", ignoreCase = true)
        if (!isTrusted && !hasUpiKeyword) return null

        // Skip OTP and promotional messages
        if (body.contains("OTP", ignoreCase = true) ||
            body.contains("one time", ignoreCase = true) ||
            body.contains("password", ignoreCase = true) ||
            body.contains("offer", ignoreCase = true) ||
            (body.contains("cashback", ignoreCase = true) && !body.contains("debited", ignoreCase = true))) {
            return null
        }

        val isDebit = body.contains("debited", ignoreCase = true) ||
                body.contains("deducted", ignoreCase = true) ||
                body.contains("paid to", ignoreCase = true) ||
                body.contains("sent to", ignoreCase = true) ||
                body.contains("payment of", ignoreCase = true)

        val isCredit = body.contains("credited", ignoreCase = true) ||
                body.contains("received", ignoreCase = true) ||
                body.contains("added to", ignoreCase = true)

        // Need at least one clear direction
        if (!isDebit && !isCredit) return null
        // Skip if ambiguous
        if (isDebit && isCredit) return null

        val amountMatch = AMOUNT_REGEX.find(body) ?: return null
        val amount = amountMatch.groupValues[1].replace(",", "").toDoubleOrNull() ?: return null
        if (amount <= 0) return null

        val merchant = VPA_REGEX.find(body)?.groupValues?.get(1)?.trim()
            ?: MERCHANT_REGEX.find(body)?.groupValues?.get(1)?.trim()
            ?: "UPI Payment"

        return ParsedTransaction(
            amount = amount,
            type = if (isDebit) "EXPENSE" else "INCOME",
            merchant = merchant.take(50)
        )
    }

    fun guessCategory(merchant: String): String {
        val m = merchant.lowercase()
        return when {
            m.containsAny("swiggy", "zomato", "blinkit", "bigbasket", "grofer", "dunzo", "food", "restaurant", "cafe", "hotel", "kitchen", "dhaba") -> "Food"
            m.containsAny("ola", "uber", "rapido", "metro", "irctc", "makemytrip", "redbus", "flight", "train", "bus", "cab", "auto", "petrol", "fuel") -> "Transport"
            m.containsAny("amazon", "flipkart", "myntra", "ajio", "nykaa", "meesho", "shop", "store", "mall", "market") -> "Shopping"
            m.containsAny("netflix", "hotstar", "prime", "spotify", "youtube", "zee5", "sonyliv", "gaana", "jiosavan", "entertainment") -> "Entertainment"
            m.containsAny("hospital", "clinic", "pharmacy", "medical", "health", "doctor", "lab", "diagnostic", "apollo", "fortis") -> "Health"
            m.containsAny("electricity", "water", "gas", "bill", "bsnl", "jio", "airtel", "vi ", "vodafone", "idea", "recharge", "utility") -> "Utilities"
            m.containsAny("school", "college", "university", "course", "udemy", "coursera", "education", "tuition", "fee") -> "Education"
            else -> "Other"
        }
    }

    private fun String.containsAny(vararg terms: String) = terms.any { this.contains(it) }
}
