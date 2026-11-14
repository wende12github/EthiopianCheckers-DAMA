# Contributing to Ethiopian Checkers (Dama / የኢትዮጵያ ዳማ)

Thank you for your interest in contributing to **Ethiopian Checkers (Dama)**! This project is an open-source initiative dedicated to preserving and digitizing traditional Ethiopian cultural board games through modern, tournament-grade software engineering.

To maintain code stability, architectural integrity, and prevent merge conflicts, please read and follow this guide carefully before submitting changes.

---

## Table of Contents
1. [Branching Strategy & Workflow](#1-branching-strategy--workflow)
2. [Step-by-Step Contribution Guide](#2-step-by-step-contribution-guide)
3. [System Architecture for Developers](#3-system-architecture-for-developers)
4. [Running the Test Suite Locally](#4-running-the-test-suite-locally)
5. [Coding & Style Standards](#5-coding--style-standards)
6. [Commit Message Conventions](#6-commit-message-conventions)
7. [Pull Request Checklist](#7-pull-request-checklist)

---

## 1. Branching Strategy & Workflow

This repository enforces a strict **Forking & Protected Branching Model**:

```
                       ┌──────────────────────────────────────────────┐
                       │               main (PROTECTED)               │
                       │   Stable Production Releases (v2.0, v2.1)    │
                       └──────────────────────▲───────────────────────┘
                                              │ (Release Merge Only)
                       ┌──────────────────────┴───────────────────────┐
                       │           development (PROTECTED)            │
                       │   Active Integration Branch (All PRs land)   │
                       └──────────────────────▲───────────────────────┘
                                              │ (Pull Request via Fork)
                       ┌──────────────────────┴───────────────────────┐
                       │      Your Fork: feature/awesome-feature      │
                       │          (Your Independent Branch)           │
                       └──────────────────────────────────────────────┘
```

* **`main` (Protected)**: Contains stable, production-ready releases (e.g. `v2.0.0`). **Direct pushes and force pushes are strictly blocked.**
* **`development` (Target for all PRs)**: The primary working integration branch. All bug fixes, enhancements, and new features must be submitted via Pull Request targeting `development`.
* **Feature Branches**: Contributors work on isolated branches created from their own fork (e.g. `feature/audio-effects`, `fix/jump-validation`).

---

## 2. Step-by-Step Contribution Guide

### Step 1: Fork the Repository
Click the **Fork** button at the top-right corner of the [EthiopianCheckers-DAMA repository](https://github.com/wende12github/EthiopianCheckers-DAMA) to create your personal copy.

### Step 2: Clone Your Fork
Clone your fork to your local machine:
```bash
git clone https://github.com/YOUR_USERNAME/EthiopianCheckers-DAMA.git
cd EthiopianCheckers-DAMA
```

### Step 3: Configure Upstream Remote
Keep your fork in sync with the official repository:
```bash
git remote add upstream https://github.com/wende12github/EthiopianCheckers-DAMA.git
git fetch upstream
```

### Step 4: Create a New Feature Branch
Always base your feature branch on the latest `development` branch:
```bash
git checkout -b feature/my-new-feature upstream/development
```

### Step 5: Make Your Changes & Test
Implement your changes following the [Coding Standards](#5-coding--style-standards). Before committing, ensure the entire test suite passes:
```bash
./mvnw clean test-compile
CP=$(find ~/.m2/repository/org/junit ~/.m2/repository/org/opentest4j ~/.m2/repository/org/apiguardian ~/.m2/repository/org/openjfx -name "*.jar" | paste -sd: -):target/classes:target/test-classes
java -cp "$CP" com.dama.ethiopiancheckersgame.TestRunner
```

### Step 6: Commit Your Work
Commit your changes using clear [Conventional Commits](#6-commit-message-conventions):
```bash
git add .
git commit -m "feat(ai): improve endgame king hunting heuristic"
```

### Step 7: Push to Your Fork & Open a Pull Request
Push your branch to your personal fork:
```bash
git push origin feature/my-new-feature
```
Then visit the original repository on GitHub. You will see a banner prompting you to **Compare & pull request**.
* **Base repository**: `wende12github/EthiopianCheckers-DAMA`
* **Base branch**: `development` *(Do NOT target `main`)*
* **Head repository**: `YOUR_USERNAME/EthiopianCheckers-DAMA`
* **Compare branch**: `feature/my-new-feature`

---

## 3. System Architecture for Developers

The codebase is organized into decoupled, single-responsibility packages. When contributing, place your code in the appropriate layer:

```
src/main/java/com/dama/ethiopiancheckersgame/
├── core/
│   ├── model/         # Immutable records & state (Position, Piece, Move, Board, GameState)
│   └── rules/         # RuleStrategy interface (EgregnaRules, TankegnaRules)
├── player/            # Player interface & implementations (HumanPlayer, NetworkPlayer)
│   └── ai/            # MinimaxEngine, Alpha-Beta pruning, BoardEvaluator
├── engine/            # GameSession turn loop, DrawDetector (insufficient material, 3-fold repetition)
├── network/           # MatchmakingServer, NetworkClient, NetworkProtocol, ServerMain
└── ui/
    ├── gui/           # JavaFX Canvas, StartMenu, GameScene, ControlPanel, Theme, TibebBanner
    ├── i18n/          # I18n localization engine, Language enum (AMHARIC, ENGLISH)
    └── cli/           # MainTerminal, TerminalGame (ANSI text interface)
```

### Architectural Principles to Respect:
1. **Separation of Game Rules from UI**: Never put rule calculations or capture validations inside JavaFX UI classes (`BoardCanvas`, `GameScene`). Rules belong strictly in `core/rules/` implementing `DamaRuleStrategy`.
2. **Strategy Pattern**: If introducing a new rule variant, implement the `DamaRuleStrategy` interface instead of adding conditional branches into existing rules.
3. **Strict Bilingual i18n**: Never hardcode English or Amharic strings directly into user-facing components. Register translation keys in [`I18n.java`](src/main/java/com/dama/ethiopiancheckersgame/ui/i18n/I18n.java) and retrieve them via `I18n.get("your.key")`.

---

## 4. Running the Test Suite Locally

Every Pull Request must pass the automated CI/CD pipeline on GitHub Actions. You can execute the test suite locally at any time:

```bash
# 1. Compile source and test classes
./mvnw clean test-compile

# 2. Run the test runner (23 unit tests)
CP=$(find ~/.m2/repository/org/junit ~/.m2/repository/org/opentest4j ~/.m2/repository/org/apiguardian ~/.m2/repository/org/openjfx -name "*.jar" | paste -sd: -):target/classes:target/test-classes
java -cp "$CP" com.dama.ethiopiancheckersgame.TestRunner
```

Expected output:
```
Running Ethiopian Checkers (Dama) v2.0 Test Suite...

==> Running EthiopianDamaRulesTest:
  ✓ [PASS] testInitialBoardSetup
  ✓ [PASS] testEgregnaRegularPieceCannotCaptureBackward
  ...
==> Running DrawDetectorTest:
  ✓ [PASS] testDrawByThreefoldRepetition
  ...
==> Running NetworkMatchmakingTest:
  ✓ [PASS] testQuickMatchPairing
  ...
==> Running I18nTest:
  ✓ [PASS] testStrictLanguageSeparation
  ...

Tests summary: 23 passed, 0 failed.
```

If you add new rules, engine capabilities, or network protocols, **you must write corresponding unit tests** in `src/test/java/`.

---

## 5. Coding & Style Standards

* **Language Level**: Java 21 LTS (use records, pattern matching, `var`, and enhanced switch expressions where appropriate).
* **Immutability**: Board coordinates (`Position`), pieces (`Piece`), and moves (`Move`) are immutable records. Do not introduce mutable state into core models.
* **Thread Safety**: Long calculations (AI minimax, socket networking) must never block the JavaFX Application Thread. Use background worker threads or `CompletableFuture`, dispatching UI updates via `Platform.runLater()`.
* **Zero External Bloat**: Keep external runtime dependencies to an absolute minimum.

---

## 6. Commit Message Conventions

We adhere to the [Conventional Commits](https://www.conventionalcommits.org/) specification:

* `feat: ...` for a new feature (e.g. `feat(network): add latency ping indicator`)
* `fix: ...` for a bug fix (e.g. `fix(rules): resolve double jump validation in Tankegna`)
* `docs: ...` for documentation additions or corrections
* `style: ...` for formatting, layout, or cosmetic changes
* `refactor: ...` for code refactoring without behavior change
* `test: ...` for adding or improving unit tests
* `ci: ...` for GitHub Actions and build script changes

---

## 7. Pull Request Checklist

Before submitting your PR, verify:
- [ ] My PR targets the **`development`** branch (not `main`).
- [ ] My code compiles cleanly with `./mvnw compile`.
- [ ] All 23 unit tests pass without failure via `TestRunner`.
- [ ] I have added unit tests for any newly introduced rule, engine, or network logic.
- [ ] User-facing strings support both Amharic and English via `I18n`.
- [ ] My commit history is clean and follows conventional commit format.

Thank you for helping preserve and elevate Ethiopian cultural games through open-source software! 🇪🇹✨
