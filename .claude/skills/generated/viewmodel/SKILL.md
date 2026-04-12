---
name: viewmodel
description: "Skill for the Viewmodel area of BankProgg. 7 symbols across 7 files."
---

# Viewmodel

7 symbols | 7 files | Cohesion: 100%

## When to Use

- Working with code in `app/`
- Understanding how HistoryViewModel, DashboardViewModel, AddTransactionViewModel work
- Modifying viewmodel-related functionality

## Key Files

| File | Symbols |
|------|---------|
| `app/src/main/java/com/financeapp/viewmodel/ViewModelFactory.kt` | create |
| `app/src/main/java/com/financeapp/viewmodel/HistoryViewModel.kt` | HistoryViewModel |
| `app/src/main/java/com/financeapp/viewmodel/DashboardViewModel.kt` | DashboardViewModel |
| `app/src/main/java/com/financeapp/viewmodel/AddTransactionViewModel.kt` | AddTransactionViewModel |
| `app/src/main/java/com/financeapp/ui/report/ReportViewModel.kt` | ReportViewModel |
| `app/src/main/java/com/financeapp/ui/pdf/ImportTransactionsViewModel.kt` | ImportTransactionsViewModel |
| `app/src/main/java/com/financeapp/ui/budget/BudgetViewModel.kt` | BudgetViewModel |

## Entry Points

Start here when exploring this area:

- **`HistoryViewModel`** (Class) — `app/src/main/java/com/financeapp/viewmodel/HistoryViewModel.kt:12`
- **`DashboardViewModel`** (Class) — `app/src/main/java/com/financeapp/viewmodel/DashboardViewModel.kt:20`
- **`AddTransactionViewModel`** (Class) — `app/src/main/java/com/financeapp/viewmodel/AddTransactionViewModel.kt:8`
- **`ReportViewModel`** (Class) — `app/src/main/java/com/financeapp/ui/report/ReportViewModel.kt:29`
- **`ImportTransactionsViewModel`** (Class) — `app/src/main/java/com/financeapp/ui/pdf/ImportTransactionsViewModel.kt:14`

## Key Symbols

| Symbol | Type | File | Line |
|--------|------|------|------|
| `HistoryViewModel` | Class | `app/src/main/java/com/financeapp/viewmodel/HistoryViewModel.kt` | 12 |
| `DashboardViewModel` | Class | `app/src/main/java/com/financeapp/viewmodel/DashboardViewModel.kt` | 20 |
| `AddTransactionViewModel` | Class | `app/src/main/java/com/financeapp/viewmodel/AddTransactionViewModel.kt` | 8 |
| `ReportViewModel` | Class | `app/src/main/java/com/financeapp/ui/report/ReportViewModel.kt` | 29 |
| `ImportTransactionsViewModel` | Class | `app/src/main/java/com/financeapp/ui/pdf/ImportTransactionsViewModel.kt` | 14 |
| `BudgetViewModel` | Class | `app/src/main/java/com/financeapp/ui/budget/BudgetViewModel.kt` | 20 |
| `create` | Method | `app/src/main/java/com/financeapp/viewmodel/ViewModelFactory.kt` | 17 |

## How to Explore

1. `gitnexus_context({name: "HistoryViewModel"})` — see callers and callees
2. `gitnexus_query({query: "viewmodel"})` — find related execution flows
3. Read key files listed above for implementation details
