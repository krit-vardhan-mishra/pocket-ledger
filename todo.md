# PocketLedger Readiness Tracker

Last updated: 2026-04-18
Source of truth: 01_PocketLedger.md

## Verdict
- Build health: PASS (`./gradlew test`, `./gradlew :app:assembleDebug`)
- Product readiness vs spec: NOT READY
- Current state: Core skeleton is implemented, but several required product features are still partial or missing.

## Progress Summary
- Architecture/Data foundation: 70%
- Screen feature completeness: 45%
- Notification + settings completeness: 20%
- Testing + CI completeness: 10%
- Overall release readiness: 40%

## Feature Checklist (Spec vs Code)

### 1) Core stack and architecture
- [x] Kotlin + Jetpack Compose app setup
- [x] Room entities (`Transaction`, `Budget`) and DAOs present
- [x] Hilt DI setup present
- [x] WorkManager scheduled from `Application`
- [ ] DataStore-backed `SettingsRepository` implemented and wired
- [ ] Domain/repository contracts fully match spec (`getCategoryTotals`, `getDailyTotals`, etc.)

### 2) Dashboard screen
- [x] Monthly income/expense cards
- [x] Net balance card + navigation to Statistics
- [x] Category chips shown
- [x] FAB opens add sheet
- [ ] "All" chip label/behavior polished (currently uses `OTHER` placeholder)
- [ ] Transactions list should be "today" as per spec (currently recent list)
- [ ] Swipe delete action wired to repository
- [ ] Tap transaction opens Edit flow
- [ ] Budget exceeded categories reflected in chip color

### 3) Add/Edit transaction bottom sheet
- [x] Amount, type toggle, note field
- [ ] Category selector UI (state exists, UI control missing)
- [ ] Date picker UI (currently always `System.currentTimeMillis()`)
- [ ] Inline validation for missing category (currently fallback to `FOOD`)
- [ ] Edit mode support (prefill + update path)

### 4) Statistics / analytics
- [x] Month navigation UI
- [x] Category breakdown calculations in ViewModel/use case
- [ ] Donut/Pie chart implementation (currently placeholder circle)
- [ ] Daily bar chart implementation
- [ ] Tap interactions (slice highlight, bar tooltip)
- [ ] Summary cards per spec (highest spend day, most spent category)
- [ ] Prevent navigating to future month (if required by product rule)

### 5) Budget settings
- [x] Category budget inputs persisted to Room
- [ ] Notification toggle persisted (currently local UI state only)
- [ ] Notification time picker + persistence
- [ ] Exceed-budget toggle persisted
- [ ] DataStore app settings model + repository + use in worker scheduler

### 6) Transaction history
- [x] List + search by note
- [ ] Group by date headers (Today, Yesterday, date)
- [ ] Filter bottom sheet (date range, category multi-select, type)
- [ ] CSV export + Android share sheet
- [ ] Delete/edit actions wired from row interaction

### 7) Daily notification worker
- [x] Worker class created and scheduled periodically
- [ ] Hilt WorkerFactory configuration in `Application` for reliable DI worker creation
- [ ] Notification channel + `NotificationCompat` posting
- [ ] Query exceeded categories and include in notification message
- [ ] Schedule at user-selected time (currently fixed 1-hour initial delay)

### 8) Testing
- [ ] Repository tests with in-memory Room DB
- [ ] ViewModel tests with MockK + coroutines-test
- [ ] Stats/Budget logic tests from spec list
- [ ] Meaningful coverage target and reporting (85%+ goal)

### 9) CI
- [ ] Add `.github/workflows/android.yml` (missing currently)
- [ ] Ensure CI runs tests and uploads reports
- [ ] Add/verify CI badge in README points to existing workflow

### 10) Cleanup and consistency
- [ ] Remove/merge placeholder component stub files in `ui/components` to avoid confusion
- [ ] Keep one canonical `AddTransactionSheet` implementation
- [ ] Align README claims with actual implementation status

---

## Prioritized Execution Plan

### P0 (must do before calling app "ready")
- [ ] Implement DataStore `SettingsRepository` + DI wiring
- [ ] Complete Add/Edit transaction flow (category selector, date picker, edit mode)
- [ ] Wire delete/edit actions in dashboard/history to repository
- [ ] Implement real charts (donut + daily bar) and required analytics summaries
- [ ] Implement real notification posting and user-time scheduling
- [ ] Add CI workflow file and basic test suite beyond templates

### P1 (important quality/features)
- [ ] History grouping + advanced filters + CSV export
- [ ] Budget exceeded visual state on dashboard chips
- [ ] Tighten month navigation/business constraints in statistics

### P2 (polish)
- [ ] Improve category labeling/icons and UX copy
- [ ] Refactor duplicated/placeholder component files
- [ ] Expand test depth and coverage reports

---

## Tracking Log
- [x] Initial gap analysis completed (2026-04-18)
- [x] Build and unit-test command validation completed (2026-04-18)
- [ ] P0 implementation started
- [ ] P1 implementation started
- [ ] P2 implementation started
- [ ] Final readiness review passed
