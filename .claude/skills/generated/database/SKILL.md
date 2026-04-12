---
name: database
description: "Skill for the Database area of BankProgg. 15 symbols across 8 files."
---

# Database

15 symbols | 8 files | Cohesion: 93%

## When to Use

- Working with code in `app/`
- Understanding how ParsedTransaction, ViewModelFactory, FinanceRepository work
- Modifying database-related functionality

## Key Files

| File | Symbols |
|------|---------|
| `app/src/main/java/com/financeapp/data/database/FinanceDatabase.kt` | transactionDao, categoryDao, budgetDao, monthlyBudgetTargetDao, getDatabase |
| `app/src/main/java/com/financeapp/utils/UpiSmsParser.kt` | ParsedTransaction, parse, guessCategory |
| `app/src/main/java/com/financeapp/receiver/SmsReceiver.kt` | onReceive, sendDetectionNotification |
| `app/src/main/java/com/financeapp/ui/HistoryFragment.kt` | setupViewModel |
| `app/src/main/java/com/financeapp/ui/DashboardFragment.kt` | setupViewModel |
| `app/src/main/java/com/financeapp/ui/AddTransactionFragment.kt` | setupViewModel |
| `app/src/main/java/com/financeapp/viewmodel/ViewModelFactory.kt` | ViewModelFactory |
| `app/src/main/java/com/financeapp/data/repository/FinanceRepository.kt` | FinanceRepository |

## Entry Points

Start here when exploring this area:

- **`ParsedTransaction`** (Class) — `app/src/main/java/com/financeapp/utils/UpiSmsParser.kt:4`
- **`ViewModelFactory`** (Class) — `app/src/main/java/com/financeapp/viewmodel/ViewModelFactory.kt:11`
- **`FinanceRepository`** (Class) — `app/src/main/java/com/financeapp/data/repository/FinanceRepository.kt:13`
- **`parse`** (Method) — `app/src/main/java/com/financeapp/utils/UpiSmsParser.kt:37`
- **`guessCategory`** (Method) — `app/src/main/java/com/financeapp/utils/UpiSmsParser.kt:85`

## Key Symbols

| Symbol | Type | File | Line |
|--------|------|------|------|
| `ParsedTransaction` | Class | `app/src/main/java/com/financeapp/utils/UpiSmsParser.kt` | 4 |
| `ViewModelFactory` | Class | `app/src/main/java/com/financeapp/viewmodel/ViewModelFactory.kt` | 11 |
| `FinanceRepository` | Class | `app/src/main/java/com/financeapp/data/repository/FinanceRepository.kt` | 13 |
| `parse` | Method | `app/src/main/java/com/financeapp/utils/UpiSmsParser.kt` | 37 |
| `guessCategory` | Method | `app/src/main/java/com/financeapp/utils/UpiSmsParser.kt` | 85 |
| `onReceive` | Method | `app/src/main/java/com/financeapp/receiver/SmsReceiver.kt` | 22 |
| `transactionDao` | Method | `app/src/main/java/com/financeapp/data/database/FinanceDatabase.kt` | 22 |
| `categoryDao` | Method | `app/src/main/java/com/financeapp/data/database/FinanceDatabase.kt` | 23 |
| `budgetDao` | Method | `app/src/main/java/com/financeapp/data/database/FinanceDatabase.kt` | 24 |
| `monthlyBudgetTargetDao` | Method | `app/src/main/java/com/financeapp/data/database/FinanceDatabase.kt` | 25 |
| `getDatabase` | Method | `app/src/main/java/com/financeapp/data/database/FinanceDatabase.kt` | 44 |
| `sendDetectionNotification` | Method | `app/src/main/java/com/financeapp/receiver/SmsReceiver.kt` | 60 |
| `setupViewModel` | Method | `app/src/main/java/com/financeapp/ui/HistoryFragment.kt` | 65 |
| `setupViewModel` | Method | `app/src/main/java/com/financeapp/ui/DashboardFragment.kt` | 62 |
| `setupViewModel` | Method | `app/src/main/java/com/financeapp/ui/AddTransactionFragment.kt` | 44 |

## Execution Flows

| Flow | Type | Steps |
|------|------|-------|
| `OnReceive → ParsedTransaction` | intra_community | 3 |
| `OnViewCreated → GetDatabase` | cross_community | 3 |
| `OnViewCreated → FinanceRepository` | cross_community | 3 |
| `OnViewCreated → TransactionDao` | cross_community | 3 |
| `OnViewCreated → CategoryDao` | cross_community | 3 |
| `OnViewCreated → GetDatabase` | cross_community | 3 |
| `OnViewCreated → FinanceRepository` | cross_community | 3 |
| `OnViewCreated → TransactionDao` | cross_community | 3 |
| `OnViewCreated → CategoryDao` | cross_community | 3 |
| `OnViewCreated → GetDatabase` | cross_community | 3 |

## Connected Areas

| Area | Connections |
|------|-------------|
| Ui | 1 calls |
| Repository | 1 calls |

## How to Explore

1. `gitnexus_context({name: "ParsedTransaction"})` — see callers and callees
2. `gitnexus_query({query: "database"})` — find related execution flows
3. Read key files listed above for implementation details
