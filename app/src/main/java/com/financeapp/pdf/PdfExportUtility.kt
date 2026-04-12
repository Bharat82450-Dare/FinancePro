package com.financeapp.pdf

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.financeapp.data.entities.Transaction
import com.financeapp.data.model.CategorySpending
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExportUtility {

    data class ReportData(
        val month: String,
        val year: String,
        val totalIncome: Double,
        val totalExpense: Double,
        val netBalance: Double,
        val categoryBreakdown: List<CategorySpending>,
        val transactions: List<Transaction>
    )

    suspend fun generateReport(context: Context, data: ReportData): Uri? =
        withContext(Dispatchers.IO) {
            try {
                val document = PDDocument()
                addCoverPage(document, data)
                addSummaryPage(document, data)
                addTransactionsPages(document, data.transactions)

                val file = getOutputFile(context, data.month, data.year)
                document.save(file)
                document.close()

                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    private fun addCoverPage(doc: PDDocument, data: ReportData) {
        val page = PDPage(PDRectangle.A4)
        doc.addPage(page)
        val stream = PDPageContentStream(doc, page)
        val width = page.mediaBox.width
        val height = page.mediaBox.height

        // Background header bar
        stream.setNonStrokingColor(0.18f, 0.42f, 0.87f) // primary blue
        stream.addRect(0f, height - 160f, width, 160f)
        stream.fill()

        // App name
        stream.setNonStrokingColor(1f, 1f, 1f)
        stream.beginText()
        stream.setFont(PDType1Font.HELVETICA_BOLD, 28f)
        stream.newLineAtOffset(60f, height - 80f)
        stream.showText("FinancePro")
        stream.endText()

        // Subtitle
        stream.beginText()
        stream.setFont(PDType1Font.HELVETICA, 14f)
        stream.newLineAtOffset(60f, height - 110f)
        stream.showText("Personal Finance Report")
        stream.endText()

        // Period
        val monthName = getMonthName(data.month.toInt())
        stream.setNonStrokingColor(0.13f, 0.13f, 0.13f)
        stream.beginText()
        stream.setFont(PDType1Font.HELVETICA_BOLD, 20f)
        stream.newLineAtOffset(60f, height - 200f)
        stream.showText("$monthName ${data.year}")
        stream.endText()

        stream.beginText()
        stream.setFont(PDType1Font.HELVETICA, 12f)
        stream.newLineAtOffset(60f, height - 225f)
        stream.showText("Generated: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())}")
        stream.endText()

        // Summary box
        stream.setNonStrokingColor(0.96f, 0.97f, 1f)
        stream.addRect(40f, height - 370f, width - 80f, 110f)
        stream.fill()

        stream.setNonStrokingColor(0.13f, 0.13f, 0.13f)
        val col1 = 60f
        val col2 = col1 + (width - 120f) / 3
        val col3 = col2 + (width - 120f) / 3

        stream.beginText()
        stream.setFont(PDType1Font.HELVETICA, 10f)
        stream.newLineAtOffset(col1, height - 280f)
        stream.showText("TOTAL INCOME")
        stream.endText()
        stream.setNonStrokingColor(0.18f, 0.67f, 0.4f)
        stream.beginText()
        stream.setFont(PDType1Font.HELVETICA_BOLD, 16f)
        stream.newLineAtOffset(col1, height - 300f)
        stream.showText("+Rs. ${"%.2f".format(data.totalIncome)}")
        stream.endText()

        stream.setNonStrokingColor(0.13f, 0.13f, 0.13f)
        stream.beginText()
        stream.setFont(PDType1Font.HELVETICA, 10f)
        stream.newLineAtOffset(col2, height - 280f)
        stream.showText("TOTAL EXPENSE")
        stream.endText()
        stream.setNonStrokingColor(0.92f, 0.26f, 0.21f)
        stream.beginText()
        stream.setFont(PDType1Font.HELVETICA_BOLD, 16f)
        stream.newLineAtOffset(col2, height - 300f)
        stream.showText("-Rs. ${"%.2f".format(data.totalExpense)}")
        stream.endText()

        stream.setNonStrokingColor(0.13f, 0.13f, 0.13f)
        stream.beginText()
        stream.setFont(PDType1Font.HELVETICA, 10f)
        stream.newLineAtOffset(col3, height - 280f)
        stream.showText("NET BALANCE")
        stream.endText()
        stream.setNonStrokingColor(0.18f, 0.42f, 0.87f)
        stream.beginText()
        stream.setFont(PDType1Font.HELVETICA_BOLD, 16f)
        stream.newLineAtOffset(col3, height - 300f)
        stream.showText("Rs. ${"%.2f".format(data.netBalance)}")
        stream.endText()

        stream.close()
    }

    private fun addSummaryPage(doc: PDDocument, data: ReportData) {
        val page = PDPage(PDRectangle.A4)
        doc.addPage(page)
        val stream = PDPageContentStream(doc, page)
        val width = page.mediaBox.width

        var y = page.mediaBox.height - 60f

        stream.setNonStrokingColor(0.13f, 0.13f, 0.13f)
        stream.beginText()
        stream.setFont(PDType1Font.HELVETICA_BOLD, 18f)
        stream.newLineAtOffset(40f, y)
        stream.showText("Spending by Category")
        stream.endText()
        y -= 30f

        // Table header
        stream.setNonStrokingColor(0.18f, 0.42f, 0.87f)
        stream.addRect(40f, y - 5f, width - 80f, 20f)
        stream.fill()
        stream.setNonStrokingColor(1f, 1f, 1f)
        stream.beginText()
        stream.setFont(PDType1Font.HELVETICA_BOLD, 11f)
        stream.newLineAtOffset(50f, y)
        stream.showText("Category")
        stream.endText()
        stream.beginText()
        stream.setFont(PDType1Font.HELVETICA_BOLD, 11f)
        stream.newLineAtOffset(width - 160f, y)
        stream.showText("Amount (Rs.)")
        stream.endText()
        y -= 25f

        // Table rows
        data.categoryBreakdown.forEachIndexed { i, item ->
            if (i % 2 == 0) {
                stream.setNonStrokingColor(0.96f, 0.97f, 1f)
                stream.addRect(40f, y - 5f, width - 80f, 18f)
                stream.fill()
            }
            stream.setNonStrokingColor(0.13f, 0.13f, 0.13f)
            stream.beginText()
            stream.setFont(PDType1Font.HELVETICA, 11f)
            stream.newLineAtOffset(50f, y)
            stream.showText(item.category)
            stream.endText()
            stream.beginText()
            stream.setFont(PDType1Font.HELVETICA, 11f)
            stream.newLineAtOffset(width - 160f, y)
            stream.showText("%.2f".format(item.total))
            stream.endText()
            y -= 22f
            if (y < 60f) {
                stream.close()
                val newPage = PDPage(PDRectangle.A4)
                doc.addPage(newPage)
                val newStream = PDPageContentStream(doc, newPage)
                y = newPage.mediaBox.height - 60f
                newStream.close()
                return
            }
        }
        stream.close()
    }

    private fun addTransactionsPages(doc: PDDocument, transactions: List<Transaction>) {
        if (transactions.isEmpty()) return

        val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        var pageIndex = 0
        var page = PDPage(PDRectangle.A4)
        doc.addPage(page)
        var stream = PDPageContentStream(doc, page)
        val width = page.mediaBox.width
        var y = page.mediaBox.height - 60f

        fun writeHeader() {
            stream.setNonStrokingColor(0.18f, 0.42f, 0.87f)
            stream.addRect(40f, y - 5f, width - 80f, 20f)
            stream.fill()
            stream.setNonStrokingColor(1f, 1f, 1f)
            val cols = listOf(50f, 130f, 330f, 430f)
            val headers = listOf("Date", "Description", "Category", "Amount")
            headers.forEachIndexed { i, h ->
                stream.beginText()
                stream.setFont(PDType1Font.HELVETICA_BOLD, 10f)
                stream.newLineAtOffset(cols[i], y)
                stream.showText(h)
                stream.endText()
            }
            y -= 25f
        }

        stream.setNonStrokingColor(0.13f, 0.13f, 0.13f)
        stream.beginText()
        stream.setFont(PDType1Font.HELVETICA_BOLD, 18f)
        stream.newLineAtOffset(40f, y)
        stream.showText("Transaction History")
        stream.endText()
        y -= 30f
        writeHeader()

        transactions.forEachIndexed { i, tx ->
            if (y < 60f) {
                stream.close()
                page = PDPage(PDRectangle.A4)
                doc.addPage(page)
                stream = PDPageContentStream(doc, page)
                y = page.mediaBox.height - 60f
                pageIndex++
                writeHeader()
            }
            if (i % 2 == 0) {
                stream.setNonStrokingColor(0.96f, 0.97f, 1f)
                stream.addRect(40f, y - 5f, width - 80f, 18f)
                stream.fill()
            }
            stream.setNonStrokingColor(0.13f, 0.13f, 0.13f)
            val cols = listOf(50f, 130f, 330f, 430f)
            val dateStr = dateFormat.format(Date(tx.date))
            val title = if (tx.title.length > 25) tx.title.substring(0, 22) + "..." else tx.title
            val sign = if (tx.type == "INCOME") "+" else "-"
            val values = listOf(dateStr, title, tx.category, "$sign%.0f".format(tx.amount))
            values.forEachIndexed { ci, v ->
                if (ci == 3) {
                    stream.setNonStrokingColor(
                        if (tx.type == "INCOME") 0.18f else 0.92f,
                        if (tx.type == "INCOME") 0.67f else 0.26f,
                        if (tx.type == "INCOME") 0.4f else 0.21f
                    )
                }
                stream.beginText()
                stream.setFont(PDType1Font.HELVETICA, 10f)
                stream.newLineAtOffset(cols[ci], y)
                stream.showText(v)
                stream.endText()
                stream.setNonStrokingColor(0.13f, 0.13f, 0.13f)
            }
            y -= 20f
        }
        stream.close()
    }

    private fun getOutputFile(context: Context, month: String, year: String): File {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            ?: context.filesDir
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "Finance_Report_${month}_$year.pdf")
    }

    private fun getMonthName(month: Int): String {
        val names = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        return if (month in 1..12) names[month - 1] else "Unknown"
    }
}
