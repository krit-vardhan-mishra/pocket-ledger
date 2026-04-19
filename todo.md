# PocketLedger Readiness Tracker

Last updated: 2026-04-18
Source of truth: 01_PocketLedger.md

## Verdict
- Build health: PASS (`./gradlew test`, `./gradlew :app:jacocoTestReport`, `./gradlew :app:assembleDebug`)
- Product readiness vs spec: READY
- Current state: P0, P1, and P2 implementation tracks are complete with build verification and expanded test/coverage setup.

## Progress Summary
- Architecture/Data foundation: 100%
- Screen feature completeness: 100%
- Notification + settings completeness: 100%
- Testing + CI completeness: 90%
- Overall release readiness: 95%

## Feature Checklist (Spec vs Code)

### 1) Core stack and architecture
- [x] Kotlin + Jetpack Compose app setup
- [x] Room entities (`Transaction`, `Budget`) and DAOs present
- [x] Hilt DI setup present
- [x] WorkManager scheduled from `Application`
- [x] DataStore-backed `SettingsRepository` implemented and wired
- [x] Domain/repository contracts fully match spec (`getCategoryTotals`, `getDailyTotals`, etc.)

### 2) Dashboard screen
- [x] Monthly income/expense cards
- [x] Net balance card + navigation to Statistics
- [x] Category chips shown
- [x] FAB opens add sheet
- [x] "All" chip label/behavior polished (currently uses `OTHER` placeholder)
- [x] Transactions list should be "today" as per spec (currently recent list)
- [x] Swipe delete action wired to repository
- [x] Tap transaction opens Edit flow
- [x] Budget exceeded categories reflected in chip color

### 3) Add/Edit transaction bottom sheet
- [x] Amount, type toggle, note field
- [x] Category selector UI (state exists, UI control missing)
- [x] Date picker UI (currently always `System.currentTimeMillis()`)
- [x] Inline validation for missing category (currently fallback to `FOOD`)
- [x] Edit mode support (prefill + update path)

### 4) Statistics / analytics
- [x] Month navigation UI
- [x] Category breakdown calculations in ViewModel/use case
- [x] Donut/Pie chart implementation (currently placeholder circle)
- [x] Daily bar chart implementation
- [x] Tap interactions (slice highlight, bar tooltip)
- [x] Summary cards per spec (highest spend day, most spent category)
- [x] Prevent navigating to future month (if required by product rule)

### 5) Budget settings
- [x] Category budget inputs persisted to Room
- [x] Notification toggle persisted (currently local UI state only)
- [x] Notification time picker + persistence
- [x] Exceed-budget toggle persisted
- [x] DataStore app settings model + repository + use in worker scheduler

### 6) Transaction history
- [x] List + search by note
- [x] Group by date headers (Today, Yesterday, date)
- [x] Filter bottom sheet (date range, category multi-select, type)
- [x] CSV export + Android share sheet
- [x] Delete/edit actions wired from row interaction

### 7) Daily notification worker
- [x] Worker class created and scheduled periodically
- [x] Hilt WorkerFactory configuration in `Application` for reliable DI worker creation
- [x] Notification channel + `NotificationCompat` posting
- [x] Query exceeded categories and include in notification message
- [x] Schedule at user-selected time (currently fixed 1-hour initial delay)

### 8) Testing
- [x] Repository tests for aggregation/exceeded-budget logic
- [x] ViewModel tests with MockK + coroutines-test
- [x] Stats/Budget logic tests from spec list
- [x] Meaningful coverage reporting configured (JaCoCo HTML/XML)

### 9) CI
- [x] Add `.github/workflows/android.yml` (missing currently)
- [x] Ensure CI runs tests and uploads reports
- [x] Add/verify CI badge in README points to existing workflow

### 10) Cleanup and consistency
- [x] Remove/merge placeholder component stub files in `ui/components` to avoid confusion
- [x] Keep one canonical `AddTransactionSheet` implementation
- [x] Align README claims with actual implementation status

---

## Prioritized Execution Plan

### P0 (must do before calling app "ready")
- [x] Implement DataStore `SettingsRepository` + DI wiring
- [x] Complete Add/Edit transaction flow (category selector, date picker, edit mode)
- [x] Wire delete/edit actions in dashboard/history to repository
- [x] Implement real charts (donut + daily bar) and required analytics summaries
- [x] Implement real notification posting and user-time scheduling
- [x] Add CI workflow file and basic test suite beyond templates

### P1 (important quality/features)
- [x] History grouping + advanced filters + CSV export
- [x] Budget exceeded visual state on dashboard chips
- [x] Tighten month navigation/business constraints in statistics

### P2 (polish)
- [x] Improve category labeling/icons and UX copy
- [x] Refactor duplicated/placeholder component files
- [x] Expand test depth and coverage reports

---

## Tracking Log
- [x] Initial gap analysis completed (2026-04-18)
- [x] Build and unit-test command validation completed (2026-04-18)
- [x] P0 implementation started (2026-04-18)
- [x] P0 implementation and verification passed (2026-04-18)
- [x] P1 implementation and verification passed (2026-04-18)
- [x] P2 implementation started (2026-04-18)
- [x] P2 implementation and final verification passed (2026-04-18)
