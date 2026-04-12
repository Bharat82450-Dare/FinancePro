---
name: report
description: "Skill for the Report area of BankProgg. 22 symbols across 6 files."
---

# Report

22 symbols | 6 files | Cohesion: 93%

## When to Use

- Working with code in `app/`
- Understanding how CategoryBreakdownAdapter, ReportData, CategorySpending work
- Modifying report-related functionality

## Key Files

| File | Symbols |
|------|---------|
| `app/src/main/java/com/financeapp/ui/report/ReportViewModel.kt` | selectPreviousMonth, selectNextMonth, generatePdfReport, getMonthStart, getMonthEnd (+3) |
| `app/src/main/java/com/financeapp/ui/report/ReportFragment.kt` | onViewCreated, setupLineChart, setupCategoryRecyclerView, handleExportClick, observeViewModel (+1) |
| `app/src/main/java/com/financeapp/ui/report/CategoryBreakdownAdapter.kt` | CategoryBreakdownAdapter, ViewHolder, onCreateViewHolder, bind, onBindViewHolder |
| `app/src/main/java/com/financeapp/pdf/PdfExportUtility.kt` | ReportData |
| `app/src/main/java/com/financeapp/data/model/QueryResults.kt` | CategorySpending |
| `app/src/main/java/com/financeapp/data/repository/FinanceRepository.kt` | getTransactionsInRange |

## Entry Points

Start here when exploring this area:

- **`CategoryBreakdownAdapter`** (Class) — `app/src/main/java/com/financeapp/ui/report/CategoryBreakdownAdapter.kt:11`
- **`ReportData`** (Class) — `app/src/main/java/com/financeapp/pdf/PdfExportUtility.kt:22`
- **`CategorySpending`** (Class) — `app/src/main/java/com/financeapp/data/model/QueryResults.kt:9`
- **`ViewHolder`** (Class) — `app/src/main/java/com/financeapp/ui/report/CategoryBreakdownAdapter.kt:20`
- **`selectPreviousMonth`** (Method) — `app/src/main/java/com/financeapp/ui/report/ReportViewModel.kt:87`

## Key Symbols

| Symbol | Type | File | Line |
|--------|------|------|------|
| `CategoryBreakdownAdapter` | Class | `app/src/main/java/com/financeapp/ui/report/CategoryBreakdownAdapter.kt` | 11 |
| `ReportData` | Class | `app/src/main/java/com/financeapp/pdf/PdfExportUtility.kt` | 22 |
| `CategorySpending` | Class | `app/src/main/java/com/financeapp/data/model/QueryResults.kt` | 9 |
| `ViewHolder` | Class | `app/src/main/java/com/financeapp/ui/report/CategoryBreakdownAdapter.kt` | 20 |
| `selectPreviousMonth` | Method | `app/src/main/java/com/financeapp/ui/report/ReportViewModel.kt` | 87 |
| `selectNextMonth` | Method | `app/src/main/java/com/financeapp/ui/report/ReportViewModel.kt` | 93 |
| `onViewCreated` | Method | `app/src/main/java/com/financeapp/ui/report/ReportFragment.kt` | 47 |
| `generatePdfReport` | Method | `app/src/main/java/com/financeapp/ui/report/ReportViewModel.kt` | 102 |
| `getTransactionsInRange` | Method | `app/src/main/java/com/financeapp/data/repository/FinanceRepository.kt` | 76 |
| `getMonthDisplay` | Method | `app/src/main/java/com/financeapp/ui/report/ReportViewModel.kt` | 158 |
| `getLast6MonthLabels` | Method | `app/src/main/java/com/financeapp/ui/report/ReportViewModel.kt` | 166 |
| `onCreateViewHolder` | Method | `app/src/main/java/com/financeapp/ui/report/CategoryBreakdownAdapter.kt` | 34 |
| `bind` | Method | `app/src/main/java/com/financeapp/ui/report/CategoryBreakdownAdapter.kt` | 23 |
| `onBindViewHolder` | Method | `app/src/main/java/com/financeapp/ui/report/CategoryBreakdownAdapter.kt` | 41 |
| `setupLineChart` | Method | `app/src/main/java/com/financeapp/ui/report/ReportFragment.kt` | 58 |
| `setupCategoryRecyclerView` | Method | `app/src/main/java/com/financeapp/ui/report/ReportFragment.kt` | 72 |
| `handleExportClick` | Method | `app/src/main/java/com/financeapp/ui/report/ReportFragment.kt` | 135 |
| `getMonthStart` | Method | `app/src/main/java/com/financeapp/ui/report/ReportViewModel.kt` | 131 |
| `getMonthEnd` | Method | `app/src/main/java/com/financeapp/ui/report/ReportViewModel.kt` | 138 |
| `buildLast6Months` | Method | `app/src/main/java/com/financeapp/ui/report/ReportViewModel.kt` | 146 |

## Execution Flows

| Flow | Type | Steps |
|------|------|-------|
| `OnViewCreated → BuildLast6Months` | cross_community | 5 |
| `OnViewCreated → CategoryBreakdownAdapter` | intra_community | 3 |
| `OnViewCreated → GetMonthDisplay` | cross_community | 3 |

## Connected Areas

| Area | Connections |
|------|-------------|
| Pdf | 1 calls |

## How to Explore

1. `gitnexus_context({name: "CategoryBreakdownAdapter"})` — see callers and callees
2. `gitnexus_query({query: "report"})` — find related execution flows
3. Read key files listed above for implementation details
