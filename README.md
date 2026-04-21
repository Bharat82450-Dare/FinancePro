# FinancePro 🚀

**FinancePro** is a powerful Android-based personal finance management application designed to give you total control over your financial life. It leverages intelligent SMS parsing to automatically track expenses, provides real-time budgeting tools, and generates comprehensive financial reports.

---

## ✨ Key Features

### 🏦 Automatic Expense Tracking
*   **Intelligent SMS Parser**: Automatically detects and categorizes financial transactions from bank/merchant SMS alerts.
*   **Manual Entry**: Easily add transactions that weren't captured via SMS.
*   **Transaction History**: A detailed, searchable log of all your income and expenses.

### 📊 Financial Insights & Reporting
*   **Bento Dashboard**: A modern, glassmorphic dashboard visualizing your current balance, recent activity, and budget status.
*   **PDF Report Generation**: Export your financial data into professional PDF reports for deeper analysis or documentation.
*   **PDF Statement Scanning**: Intelligent scanning and parsing of bank statements in PDF format.
*   **Daily Snapshots**: Automated background tracking of your daily financial health.

### 🎯 Budgeting & Goals
*   **Category-wise Budgets**: Set and monitor spending limits for different categories (e.g., Food, Transport, Entertainment).
*   **Savings Goals**: Define and track progress towards your financial milestones.
*   **Plan Tracker**: Sophisticated financial planning and goal tracking logic.

### 🛡️ Security & Performance
*   **Biometric Login**: Secure your financial data with fingerprint or face unlock.
*   **PIN Authentication**: Custom PIN-based security for quick and safe access.
*   **Offline-First**: Powered by Room Database for a fast, responsive experience that works without an active internet connection.

---

## 🛠️ Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/)
- **Architecture**: MVVM (Model-View-ViewModel)
- **UI Components**: XML Layouts with Material Design 3, ViewBinding
- **Database**: [Room Persistence Library](https://developer.android.com/training/data-storage/room)
- **Background Tasks**: [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
- **PDF Generation**: Native Android PDF APIs / iText (integrated in `pdf` module)
- **Security**: Biometric API & SharedPreferences

---

## 📁 Project Structure

```text
com.financeapp
├── budget      # Budgeting & Planning logic (BalanceManager, BudgetManager)
├── data        # Persistence layer (Room DB, Entities, Repositories)
├── pdf         # PDF generation and export functionality
├── receiver    # SMS BroadcastReceiver for automatic tracking
├── ui          # UI Components (Activities, Fragments, Adapters)
├── utils       # Helper classes and extensions
└── viewmodel   # Business logic and UI state management
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Iguana or newer
- JDK 17
- Android SDK 31+

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/Bharat82450-Dare/FinancePro.git
   ```
2. Open the project in Android Studio.
3. Sync Gradle and build the project.
4. Run the app on an Emulator or Physical Device.

---

## 🔒 Permissions
- `RECEIVE_SMS`: To automatically detect transaction updates.
- `READ_SMS`: To parse historical transaction messages.
- `USE_BIOMETRIC`: For secure app access.
- `POST_NOTIFICATIONS`: For budget alerts and transaction confirmations.

---

## 🎨 UI Preview
*The app features a modern, premium design with a focus on usability and visual clarity.*

---

## 📄 License
This project is licensed under the MIT License - see the LICENSE file for details.
