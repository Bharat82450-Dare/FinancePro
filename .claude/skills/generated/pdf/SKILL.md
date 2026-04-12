---
name: pdf
description: "Skill for the Pdf area of BankProgg. 28 symbols across 5 files."
---

# Pdf

28 symbols | 5 files | Cohesion: 89%

## When to Use

- Working with code in `app/`
- Understanding how ImportState, Preview, Error work
- Modifying pdf-related functionality

## Key Files

| File | Symbols |
|------|---------|
| `app/src/main/java/com/financeapp/pdf/PdfExportUtility.kt` | generateReport, addCoverPage, addSummaryPage, addTransactionsPages, writeHeader (+2) |
| `app/src/main/java/com/financeapp/pdf/PdfStatementScanner.kt` | extractTransactions, ExtractedTransaction, parsePdfText, parseDate, parseAmount (+1) |
| `app/src/main/java/com/financeapp/ui/pdf/ImportTransactionsViewModel.kt` | ImportState, Preview, Error, processPdf, saveSelectedTransactions (+1) |
| `app/src/main/java/com/financeapp/ui/pdf/ExtractedTransactionAdapter.kt` | ExtractedTransactionAdapter, update, ViewHolder, onCreateViewHolder, bind (+1) |
| `app/src/main/java/com/financeapp/ui/pdf/ImportTransactionsFragment.kt` | onViewCreated, observeViewModel, updatePreview |

## Entry Points

Start here when exploring this area:

- **`ImportState`** (Class) — `app/src/main/java/com/financeapp/ui/pdf/ImportTransactionsViewModel.kt:18`
- **`Preview`** (Class) — `app/src/main/java/com/financeapp/ui/pdf/ImportTransactionsViewModel.kt:21`
- **`Error`** (Class) — `app/src/main/java/com/financeapp/ui/pdf/ImportTransactionsViewModel.kt:22`
- **`ExtractedTransactionAdapter`** (Class) — `app/src/main/java/com/financeapp/ui/pdf/ExtractedTransactionAdapter.kt:12`
- **`ExtractedTransaction`** (Class) — `app/src/main/java/com/financeapp/pdf/PdfStatementScanner.kt:13`

## Key Symbols

| Symbol | Type | File | Line |
|--------|------|------|------|
| `ImportState` | Class | `app/src/main/java/com/financeapp/ui/pdf/ImportTransactionsViewModel.kt` | 18 |
| `Preview` | Class | `app/src/main/java/com/financeapp/ui/pdf/ImportTransactionsViewModel.kt` | 21 |
| `Error` | Class | `app/src/main/java/com/financeapp/ui/pdf/ImportTransactionsViewModel.kt` | 22 |
| `ExtractedTransactionAdapter` | Class | `app/src/main/java/com/financeapp/ui/pdf/ExtractedTransactionAdapter.kt` | 12 |
| `ExtractedTransaction` | Class | `app/src/main/java/com/financeapp/pdf/PdfStatementScanner.kt` | 13 |
| `ViewHolder` | Class | `app/src/main/java/com/financeapp/ui/pdf/ExtractedTransactionAdapter.kt` | 20 |
| `generateReport` | Method | `app/src/main/java/com/financeapp/pdf/PdfExportUtility.kt` | 32 |
| `extractTransactions` | Method | `app/src/main/java/com/financeapp/pdf/PdfStatementScanner.kt` | 47 |
| `processPdf` | Method | `app/src/main/java/com/financeapp/ui/pdf/ImportTransactionsViewModel.kt` | 33 |
| `saveSelectedTransactions` | Method | `app/src/main/java/com/financeapp/ui/pdf/ImportTransactionsViewModel.kt` | 55 |
| `toggleSelection` | Method | `app/src/main/java/com/financeapp/ui/pdf/ImportTransactionsViewModel.kt` | 49 |
| `onViewCreated` | Method | `app/src/main/java/com/financeapp/ui/pdf/ImportTransactionsFragment.kt` | 46 |
| `update` | Method | `app/src/main/java/com/financeapp/ui/pdf/ExtractedTransactionAdapter.kt` | 51 |
| `onCreateViewHolder` | Method | `app/src/main/java/com/financeapp/ui/pdf/ExtractedTransactionAdapter.kt` | 38 |
| `bind` | Method | `app/src/main/java/com/financeapp/ui/pdf/ExtractedTransactionAdapter.kt` | 23 |
| `onBindViewHolder` | Method | `app/src/main/java/com/financeapp/ui/pdf/ExtractedTransactionAdapter.kt` | 45 |
| `addCoverPage` | Method | `app/src/main/java/com/financeapp/pdf/PdfExportUtility.kt` | 51 |
| `addSummaryPage` | Method | `app/src/main/java/com/financeapp/pdf/PdfExportUtility.kt` | 144 |
| `addTransactionsPages` | Method | `app/src/main/java/com/financeapp/pdf/PdfExportUtility.kt` | 209 |
| `writeHeader` | Method | `app/src/main/java/com/financeapp/pdf/PdfExportUtility.kt` | 220 |

## Execution Flows

| Flow | Type | Steps |
|------|------|-------|
| `OnViewCreated → UpdateTransaction` | cross_community | 5 |
| `OnViewCreated → InsertTransaction` | cross_community | 4 |
| `ProcessPdf → ParseDate` | cross_community | 4 |
| `ProcessPdf → ParseAmount` | cross_community | 4 |
| `ProcessPdf → InferType` | cross_community | 4 |
| `ProcessPdf → ExtractedTransaction` | cross_community | 4 |
| `OnViewCreated → Error` | cross_community | 3 |
| `OnViewCreated → Transaction` | cross_community | 3 |
| `GenerateReport → GetMonthName` | intra_community | 3 |
| `GenerateReport → WriteHeader` | intra_community | 3 |

## Connected Areas

| Area | Connections |
|------|-------------|
| Repository | 1 calls |
| Ui | 1 calls |

## How to Explore

1. `gitnexus_context({name: "ImportState"})` — see callers and callees
2. `gitnexus_query({query: "pdf"})` — find related execution flows
3. Read key files listed above for implementation details
