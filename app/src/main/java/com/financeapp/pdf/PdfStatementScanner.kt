package com.financeapp.pdf

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

object PdfStatementScanner {

    data class ExtractedTransaction(
        val date: Long,
        val description: String,
        val amount: Double,
        val type: String // "INCOME" or "EXPENSE"
    )

    private val DATE_PATTERNS = listOf(
        Regex("""(\d{2}/\d{2}/\d{4})"""),
        Regex("""(\d{2}-\d{2}-\d{4})"""),
        Regex("""(\d{2}/\d{2}/\d{2})"""),
        Regex("""(\d{2}\.\d{2}\.\d{4})"""),
        Regex("""(\d{4}-\d{2}-\d{2})""")
    )

    private val DATE_FORMATS = listOf(
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()),
        SimpleDateFormat("dd/MM/yy", Locale.getDefault()),
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    )

    private val AMOUNT_PATTERN = Regex("""(\d{1,3}(?:[,\d]*)?(?:\.\d{2}))""")

    private val CREDIT_KEYWORDS = listOf(
        "cr", "credit", "deposit", "salary", "refund",
        "received", "inward", "neft cr", "imps cr", "upi cr"
    )
    private val DEBIT_KEYWORDS = listOf(
        "dr", "debit", "withdrawal", "payment", "purchase",
        "paid", "outward", "neft dr", "imps dr", "upi dr"
    )

    suspend fun extractTransactions(context: Context, uri: Uri): List<ExtractedTransaction> =
        withContext(Dispatchers.IO) {
            val results = mutableListOf<ExtractedTransaction>()
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val document = PDDocument.load(inputStream)
                    val stripper = PDFTextStripper()
                    val text = stripper.getText(document)
                    document.close()
                    results.addAll(parsePdfText(text))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            results
        }

    private fun parsePdfText(text: String): List<ExtractedTransaction> {
        val results = mutableListOf<ExtractedTransaction>()
        val lines = text.lines()

        for (i in lines.indices) {
            val line = lines[i].trim()
            if (line.isBlank()) continue

            val dateMatch = DATE_PATTERNS.mapNotNull { it.find(line) }.firstOrNull() ?: continue
            val dateStr = dateMatch.value
            val date = parseDate(dateStr) ?: continue

            // Look for amount in current line or next line
            val searchText = if (i + 1 < lines.size) line + " " + lines[i + 1] else line
            val amountMatches = AMOUNT_PATTERN.findAll(searchText).toList()
            if (amountMatches.isEmpty()) continue

            // Use the last amount match (usually the transaction amount in bank statements)
            val amountStr = amountMatches.last().value
            val amount = parseAmount(amountStr) ?: continue
            if (amount <= 0.01) continue

            // Extract description: text between date and first amount
            val firstAmountPos = AMOUNT_PATTERN.find(line)?.range?.first ?: line.length
            val afterDate = line.substring(dateMatch.range.last + 1).trim()
            val description = if (firstAmountPos > dateMatch.range.last + 1) {
                afterDate.substringBefore(AMOUNT_PATTERN.find(afterDate)?.value ?: "").trim()
                    .take(60).ifEmpty { "Transaction" }
            } else {
                afterDate.take(60).ifEmpty { "Transaction" }
            }

            val type = inferType(searchText.lowercase())

            results.add(ExtractedTransaction(date, description, amount, type))
        }

        return results
    }

    private fun parseDate(dateStr: String): Long? {
        for (format in DATE_FORMATS) {
            try {
                return format.parse(dateStr)?.time
            } catch (_: Exception) {}
        }
        return null
    }

    private fun parseAmount(amountStr: String): Double? {
        return try {
            // Remove commas used as thousands separators
            amountStr.replace(",", "").toDoubleOrNull()
        } catch (_: Exception) {
            null
        }
    }

    private fun inferType(lineLower: String): String {
        val creditScore = CREDIT_KEYWORDS.count { lineLower.contains(it) }
        val debitScore = DEBIT_KEYWORDS.count { lineLower.contains(it) }
        return when {
            creditScore > debitScore -> "INCOME"
            debitScore > creditScore -> "EXPENSE"
            else -> "EXPENSE" // default to expense when ambiguous
        }
    }
}
