---
name: budget
description: "Skill for the Budget area of BankProgg. 39 symbols across 10 files."
---

# Budget

39 symbols | 10 files | Cohesion: 83%

## When to Use

- Working with code in `app/`
- Understanding how MonthlyBudgetTarget, OverallBudgetSummary, BudgetWithStatus work
- Modifying budget-related functionality

## Key Files

| File | Symbols |
|------|---------|
| `app/src/main/java/com/financeapp/ui/budget/BudgetFragment.kt` | showSetTargetDialog, showAddBudgetDialog, onViewCreated, setupRecyclerView, observeViewModel (+4) |
| `app/src/main/java/com/financeapp/data/repository/FinanceRepository.kt` | insertMonthlyTarget, getTotalExpenseForMonth, getMonthlyTargetForMonth, getCategorySpendingForMonth, getBudgetsForMonth (+2) |
| `app/src/main/java/com/financeapp/budget/BudgetManager.kt` | OverallBudgetSummary, getOverallBudgetSummary, sendOverallExceededNotification, BudgetWithStatus, getBudgetStatuses (+1) |
| `app/src/main/java/com/financeapp/ui/budget/BudgetAdapter.kt` | BudgetAdapter, BudgetViewHolder, onCreateViewHolder, bind, onBindViewHolder |
| `app/src/main/java/com/financeapp/budget/BudgetStatus.kt` | BudgetStatus, Ok, Warning, Exceeded |
| `app/src/main/java/com/financeapp/ui/budget/BudgetViewModel.kt` | setMonthlyTarget, addBudget, deleteBudget |
| `app/src/main/java/com/financeapp/data/dao/SharedDao.kt` | insertTarget, getTargetForMonth |
| `app/src/main/java/com/financeapp/utils/SessionManager.kt` | getUserEmail |
| `app/src/main/java/com/financeapp/data/entities/MonthlyBudgetTarget.kt` | MonthlyBudgetTarget |
| `app/src/main/java/com/financeapp/data/entities/Budget.kt` | Budget |

## Entry Points

Start here when exploring this area:

- **`MonthlyBudgetTarget`** (Class) — `app/src/main/java/com/financeapp/data/entities/MonthlyBudgetTarget.kt:5`
- **`OverallBudgetSummary`** (Class) — `app/src/main/java/com/financeapp/budget/BudgetManager.kt:22`
- **`BudgetWithStatus`** (Class) — `app/src/main/java/com/financeapp/budget/BudgetManager.kt:16`
- **`Budget`** (Class) — `app/src/main/java/com/financeapp/data/entities/Budget.kt:5`
- **`BudgetAdapter`** (Class) — `app/src/main/java/com/financeapp/ui/budget/BudgetAdapter.kt:14`

## Key Symbols

| Symbol | Type | File | Line |
|--------|------|------|------|
| `MonthlyBudgetTarget` | Class | `app/src/main/java/com/financeapp/data/entities/MonthlyBudgetTarget.kt` | 5 |
| `OverallBudgetSummary` | Class | `app/src/main/java/com/financeapp/budget/BudgetManager.kt` | 22 |
| `BudgetWithStatus` | Class | `app/src/main/java/com/financeapp/budget/BudgetManager.kt` | 16 |
| `Budget` | Class | `app/src/main/java/com/financeapp/data/entities/Budget.kt` | 5 |
| `BudgetAdapter` | Class | `app/src/main/java/com/financeapp/ui/budget/BudgetAdapter.kt` | 14 |
| `BudgetStatus` | Class | `app/src/main/java/com/financeapp/budget/BudgetStatus.kt` | 2 |
| `Ok` | Class | `app/src/main/java/com/financeapp/budget/BudgetStatus.kt` | 3 |
| `Warning` | Class | `app/src/main/java/com/financeapp/budget/BudgetStatus.kt` | 9 |
| `Exceeded` | Class | `app/src/main/java/com/financeapp/budget/BudgetStatus.kt` | 15 |
| `BudgetViewHolder` | Class | `app/src/main/java/com/financeapp/ui/budget/BudgetAdapter.kt` | 18 |
| `getUserEmail` | Method | `app/src/main/java/com/financeapp/utils/SessionManager.kt` | 50 |
| `setMonthlyTarget` | Method | `app/src/main/java/com/financeapp/ui/budget/BudgetViewModel.kt` | 70 |
| `insertMonthlyTarget` | Method | `app/src/main/java/com/financeapp/data/repository/FinanceRepository.kt` | 101 |
| `insertTarget` | Method | `app/src/main/java/com/financeapp/data/dao/SharedDao.kt` | 37 |
| `getOverallBudgetSummary` | Method | `app/src/main/java/com/financeapp/budget/BudgetManager.kt` | 61 |
| `getTotalExpenseForMonth` | Method | `app/src/main/java/com/financeapp/data/repository/FinanceRepository.kt` | 73 |
| `getMonthlyTargetForMonth` | Method | `app/src/main/java/com/financeapp/data/repository/FinanceRepository.kt` | 95 |
| `getTargetForMonth` | Method | `app/src/main/java/com/financeapp/data/dao/SharedDao.kt` | 40 |
| `getBudgetStatuses` | Method | `app/src/main/java/com/financeapp/budget/BudgetManager.kt` | 36 |
| `getCategorySpendingForMonth` | Method | `app/src/main/java/com/financeapp/data/repository/FinanceRepository.kt` | 70 |

## Execution Flows

| Flow | Type | Steps |
|------|------|-------|
| `OnViewCreated → SaveSmsTrackingEnabled` | cross_community | 4 |
| `ShowSetTargetDialog → InsertTarget` | intra_community | 4 |
| `OnViewCreated → BudgetAdapter` | intra_community | 3 |
| `OnViewCreated → DeleteBudget` | intra_community | 3 |
| `OnViewCreated → IsSmsTrackingEnabled` | cross_community | 3 |
| `ShowSetTargetDialog → GetUserEmail` | intra_community | 3 |
| `ShowSetTargetDialog → MonthlyBudgetTarget` | intra_community | 3 |
| `ShowAddBudgetDialog → GetUserEmail` | cross_community | 3 |
| `ShowAddBudgetDialog → GetBudgetByCategory` | intra_community | 3 |
| `ShowAddBudgetDialog → InsertBudget` | intra_community | 3 |

## How to Explore

1. `gitnexus_context({name: "MonthlyBudgetTarget"})` — see callers and callees
2. `gitnexus_query({query: "budget"})` — find related execution flows
3. Read key files listed above for implementation details
