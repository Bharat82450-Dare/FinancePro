---
name: repository
description: "Skill for the Repository area of BankProgg. 11 symbols across 4 files."
---

# Repository

11 symbols | 4 files | Cohesion: 87%

## When to Use

- Working with code in `app/`
- Understanding how syncData, addTransaction, syncPendingData work
- Modifying repository-related functionality

## Key Files

| File | Symbols |
|------|---------|
| `app/src/main/java/com/financeapp/data/repository/FinanceRepository.kt` | addTransaction, syncToFirestore, syncPendingData, addCategory, getMonthlyTargetOnce |
| `app/src/main/java/com/financeapp/data/dao/TransactionDao.kt` | insertTransaction, updateTransaction, getUnsyncedTransactions |
| `app/src/main/java/com/financeapp/data/dao/SharedDao.kt` | insertCategory, getTargetForMonthOnce |
| `app/src/main/java/com/financeapp/viewmodel/DashboardViewModel.kt` | syncData |

## Entry Points

Start here when exploring this area:

- **`syncData`** (Method) — `app/src/main/java/com/financeapp/viewmodel/DashboardViewModel.kt:49`
- **`addTransaction`** (Method) — `app/src/main/java/com/financeapp/data/repository/FinanceRepository.kt:25`
- **`syncPendingData`** (Method) — `app/src/main/java/com/financeapp/data/repository/FinanceRepository.kt:62`
- **`insertTransaction`** (Method) — `app/src/main/java/com/financeapp/data/dao/TransactionDao.kt:17`
- **`updateTransaction`** (Method) — `app/src/main/java/com/financeapp/data/dao/TransactionDao.kt:20`

## Key Symbols

| Symbol | Type | File | Line |
|--------|------|------|------|
| `syncData` | Method | `app/src/main/java/com/financeapp/viewmodel/DashboardViewModel.kt` | 49 |
| `addTransaction` | Method | `app/src/main/java/com/financeapp/data/repository/FinanceRepository.kt` | 25 |
| `syncPendingData` | Method | `app/src/main/java/com/financeapp/data/repository/FinanceRepository.kt` | 62 |
| `insertTransaction` | Method | `app/src/main/java/com/financeapp/data/dao/TransactionDao.kt` | 17 |
| `updateTransaction` | Method | `app/src/main/java/com/financeapp/data/dao/TransactionDao.kt` | 20 |
| `getUnsyncedTransactions` | Method | `app/src/main/java/com/financeapp/data/dao/TransactionDao.kt` | 26 |
| `addCategory` | Method | `app/src/main/java/com/financeapp/data/repository/FinanceRepository.kt` | 81 |
| `insertCategory` | Method | `app/src/main/java/com/financeapp/data/dao/SharedDao.kt` | 9 |
| `getMonthlyTargetOnce` | Method | `app/src/main/java/com/financeapp/data/repository/FinanceRepository.kt` | 98 |
| `getTargetForMonthOnce` | Method | `app/src/main/java/com/financeapp/data/dao/SharedDao.kt` | 43 |
| `syncToFirestore` | Method | `app/src/main/java/com/financeapp/data/repository/FinanceRepository.kt` | 30 |

## Execution Flows

| Flow | Type | Steps |
|------|------|-------|
| `OnViewCreated → UpdateTransaction` | cross_community | 5 |
| `OnViewCreated → InsertTransaction` | cross_community | 4 |
| `SyncData → UpdateTransaction` | intra_community | 4 |
| `SyncData → GetUnsyncedTransactions` | intra_community | 3 |

## How to Explore

1. `gitnexus_context({name: "syncData"})` — see callers and callees
2. `gitnexus_query({query: "repository"})` — find related execution flows
3. Read key files listed above for implementation details
