---
name: ui
description: "Skill for the Ui area of BankProgg. 49 symbols across 13 files."
---

# Ui

49 symbols | 13 files | Cohesion: 85%

## When to Use

- Working with code in `app/`
- Understanding how PinFragment, LoginFragment, Transaction work
- Modifying ui-related functionality

## Key Files

| File | Symbols |
|------|---------|
| `app/src/main/java/com/financeapp/ui/PinFragment.kt` | setupKeypad, collectButtons, handleFullPin, updateDots, onAuthenticationSucceeded (+5) |
| `app/src/main/java/com/financeapp/utils/SessionManager.kt` | setPin, setBiometricEnabled, getPin, isPinSet, isBiometricEnabled (+1) |
| `app/src/main/java/com/financeapp/ui/DashboardFragment.kt` | onViewCreated, setupRecyclerView, setupChart, observeViewModel, updateBalance (+1) |
| `app/src/main/java/com/financeapp/ui/HistoryFragment.kt` | onViewCreated, setupMenu, setupUI, observeViewModel, onMenuItemSelected (+1) |
| `app/src/main/java/com/financeapp/ui/AuthActivity.kt` | onCreate, showPin, showLogin, showSignup |
| `app/src/main/java/com/financeapp/ui/AddTransactionFragment.kt` | onViewCreated, setupUI, showDatePicker, saveTransaction |
| `app/src/main/java/com/financeapp/ui/TransactionAdapter.kt` | onCreateViewHolder, TransactionViewHolder, onBindViewHolder, bind |
| `app/src/main/java/com/financeapp/ui/SignupFragment.kt` | onViewCreated, SignupFragment |
| `app/src/main/java/com/financeapp/ui/LoginFragment.kt` | LoginFragment, onViewCreated |
| `app/src/main/java/com/financeapp/viewmodel/HistoryViewModel.kt` | updateFilterType, updateSearchQuery |

## Entry Points

Start here when exploring this area:

- **`PinFragment`** (Class) — `app/src/main/java/com/financeapp/ui/PinFragment.kt:21`
- **`LoginFragment`** (Class) — `app/src/main/java/com/financeapp/ui/LoginFragment.kt:17`
- **`Transaction`** (Class) — `app/src/main/java/com/financeapp/data/entities/Transaction.kt:5`
- **`SignupFragment`** (Class) — `app/src/main/java/com/financeapp/ui/SignupFragment.kt:14`
- **`TransactionViewHolder`** (Class) — `app/src/main/java/com/financeapp/ui/TransactionAdapter.kt:26`

## Key Symbols

| Symbol | Type | File | Line |
|--------|------|------|------|
| `PinFragment` | Class | `app/src/main/java/com/financeapp/ui/PinFragment.kt` | 21 |
| `LoginFragment` | Class | `app/src/main/java/com/financeapp/ui/LoginFragment.kt` | 17 |
| `Transaction` | Class | `app/src/main/java/com/financeapp/data/entities/Transaction.kt` | 5 |
| `SignupFragment` | Class | `app/src/main/java/com/financeapp/ui/SignupFragment.kt` | 14 |
| `TransactionViewHolder` | Class | `app/src/main/java/com/financeapp/ui/TransactionAdapter.kt` | 26 |
| `ImportTransactionsFragment` | Class | `app/src/main/java/com/financeapp/ui/pdf/ImportTransactionsFragment.kt` | 19 |
| `setPin` | Method | `app/src/main/java/com/financeapp/utils/SessionManager.kt` | 57 |
| `setBiometricEnabled` | Method | `app/src/main/java/com/financeapp/utils/SessionManager.kt` | 79 |
| `getPin` | Method | `app/src/main/java/com/financeapp/utils/SessionManager.kt` | 65 |
| `isPinSet` | Method | `app/src/main/java/com/financeapp/utils/SessionManager.kt` | 72 |
| `isBiometricEnabled` | Method | `app/src/main/java/com/financeapp/utils/SessionManager.kt` | 87 |
| `onViewCreated` | Method | `app/src/main/java/com/financeapp/ui/PinFragment.kt` | 41 |
| `onViewCreated` | Method | `app/src/main/java/com/financeapp/ui/SignupFragment.kt` | 34 |
| `onCreate` | Method | `app/src/main/java/com/financeapp/ui/AuthActivity.kt` | 15 |
| `showPin` | Method | `app/src/main/java/com/financeapp/ui/AuthActivity.kt` | 42 |
| `showLogin` | Method | `app/src/main/java/com/financeapp/ui/AuthActivity.kt` | 54 |
| `onViewCreated` | Method | `app/src/main/java/com/financeapp/ui/DashboardFragment.kt` | 40 |
| `onViewCreated` | Method | `app/src/main/java/com/financeapp/ui/AddTransactionFragment.kt` | 37 |
| `addTransaction` | Method | `app/src/main/java/com/financeapp/viewmodel/AddTransactionViewModel.kt` | 10 |
| `onViewCreated` | Method | `app/src/main/java/com/financeapp/ui/HistoryFragment.kt` | 41 |

## Execution Flows

| Flow | Type | Steps |
|------|------|-------|
| `OnViewCreated → Transaction` | intra_community | 5 |
| `OnCreate → PinFragment` | intra_community | 3 |
| `OnViewCreated → GetPin` | intra_community | 3 |
| `OnViewCreated → GetDatabase` | cross_community | 3 |
| `OnViewCreated → FinanceRepository` | cross_community | 3 |
| `OnViewCreated → TransactionDao` | cross_community | 3 |
| `OnViewCreated → CategoryDao` | cross_community | 3 |
| `OnViewCreated → GetDatabase` | cross_community | 3 |
| `OnViewCreated → FinanceRepository` | cross_community | 3 |
| `OnViewCreated → TransactionDao` | cross_community | 3 |

## Connected Areas

| Area | Connections |
|------|-------------|
| Database | 3 calls |
| Financeapp | 2 calls |
| Repository | 1 calls |

## How to Explore

1. `gitnexus_context({name: "PinFragment"})` — see callers and callees
2. `gitnexus_query({query: "ui"})` — find related execution flows
3. Read key files listed above for implementation details
