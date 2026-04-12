---
name: financeapp
description: "Skill for the Financeapp area of BankProgg. 9 symbols across 4 files."
---

# Financeapp

9 symbols | 4 files | Cohesion: 89%

## When to Use

- Working with code in `app/`
- Understanding how SessionManager, onCreate, onCreate work
- Modifying financeapp-related functionality

## Key Files

| File | Symbols |
|------|---------|
| `app/src/main/java/com/financeapp/MainActivity.kt` | onCreate, requestNotificationPermission, onOptionsItemSelected, performLogout |
| `app/src/main/java/com/financeapp/utils/SessionManager.kt` | SessionManager, logout |
| `app/src/main/java/com/financeapp/FinanceApplication.kt` | onCreate, createNotificationChannel |
| `app/src/main/java/com/financeapp/ui/SplashActivity.kt` | onCreate |

## Entry Points

Start here when exploring this area:

- **`SessionManager`** (Class) — `app/src/main/java/com/financeapp/utils/SessionManager.kt:9`
- **`onCreate`** (Method) — `app/src/main/java/com/financeapp/MainActivity.kt:24`
- **`onCreate`** (Method) — `app/src/main/java/com/financeapp/ui/SplashActivity.kt:17`
- **`onOptionsItemSelected`** (Method) — `app/src/main/java/com/financeapp/MainActivity.kt:46`
- **`logout`** (Method) — `app/src/main/java/com/financeapp/utils/SessionManager.kt:42`

## Key Symbols

| Symbol | Type | File | Line |
|--------|------|------|------|
| `SessionManager` | Class | `app/src/main/java/com/financeapp/utils/SessionManager.kt` | 9 |
| `onCreate` | Method | `app/src/main/java/com/financeapp/MainActivity.kt` | 24 |
| `onCreate` | Method | `app/src/main/java/com/financeapp/ui/SplashActivity.kt` | 17 |
| `onOptionsItemSelected` | Method | `app/src/main/java/com/financeapp/MainActivity.kt` | 46 |
| `logout` | Method | `app/src/main/java/com/financeapp/utils/SessionManager.kt` | 42 |
| `onCreate` | Method | `app/src/main/java/com/financeapp/FinanceApplication.kt` | 9 |
| `requestNotificationPermission` | Method | `app/src/main/java/com/financeapp/MainActivity.kt` | 66 |
| `performLogout` | Method | `app/src/main/java/com/financeapp/MainActivity.kt` | 56 |
| `createNotificationChannel` | Method | `app/src/main/java/com/financeapp/FinanceApplication.kt` | 15 |

## Execution Flows

| Flow | Type | Steps |
|------|------|-------|
| `OnOptionsItemSelected → Logout` | intra_community | 3 |

## How to Explore

1. `gitnexus_context({name: "SessionManager"})` — see callers and callees
2. `gitnexus_query({query: "financeapp"})` — find related execution flows
3. Read key files listed above for implementation details
