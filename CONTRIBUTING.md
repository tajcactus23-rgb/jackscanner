# Contributing to BlueMeanie

Thank you for your interest in contributing to BlueMeanie!

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Making Changes](#making-changes)
- [Pull Request Process](#pull-request-process)
- [Style Guides](#style-guides)

---

## Code of Conduct

This project adheres to a code of conduct that all contributors are expected to follow. Please be:

- **Respectful** - Treat others with respect
- **Constructive** - Provide helpful feedback
- **Inclusive** - Welcome diverse perspectives
- **Patient** - Help newcomers learn

---

## Getting Started

1. **Fork the repository** on GitHub
2. **Clone your fork** locally:
   ```bash
   git clone https://github.com/YOUR_USERNAME/jackscanner.git
   cd jackscanner
   ```

3. **Add upstream remote**:
   ```bash
   git remote add upstream https://github.com/tajcactus23-rgb/jackscanner.git
   ```

4. **Create a branch** for your changes:
   ```bash
   git checkout -b feature/your-feature-name
   ```

---

## Development Setup

### Prerequisites

- Android Studio Hedgehog (2024.1.1) or later
- JDK 17
- Android SDK 34
- Git

### Environment Setup

1. **Import project** in Android Studio
2. **Sync Gradle** files
3. **Build debug APK** to verify setup:
   ```bash
   cd jackscanner
   ./gradlew assembleDebug
   ```

### Running Tests

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest

# Lint checks
./gradlew lint
```

---

## Making Changes

### 1. Create a Branch

Always work on a feature branch:
```bash
git checkout -b feature/add-new-detection-method
```

### 2. Make Your Changes

- Write clean, maintainable code
- Follow the style guides below
- Add tests for new functionality
- Update documentation as needed

### 3. Commit Your Changes

Use conventional commit messages:
```bash
git commit -m "feat: add new detection algorithm"
git commit -m "fix: resolve Bluetooth scanning issue"
git commit -m "docs: update README with new features"
```

### 4. Push and Create PR

```bash
git push origin feature/your-feature-name
```

Then create a Pull Request on GitHub.

---

## Pull Request Process

### Before Submitting

1. ✅ Run all tests locally
2. ✅ Ensure code follows style guidelines
3. ✅ Update CHANGELOG if applicable
4. ✅ Write clear PR description

### PR Template

```markdown
## Description
Brief description of what this PR does

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Documentation update
- [ ] Code refactoring

## Testing
Describe how you tested your changes

## Screenshots (if UI changes)
Add screenshots of before/after if applicable

## Checklist
- [ ] My code follows the style guidelines
- [ ] I have performed a self-review
- [ ] I have commented my code where necessary
- [ ] My changes generate no new warnings
- [ ] I have added tests that prove my fix works
```

---

## Style Guides

### Kotlin

- Use **4 spaces** for indentation (no tabs)
- Use **camelCase** for variable names
- Use **PascalCase** for class and function names
- Maximum line length: **120 characters**
- Add documentation for public APIs

### Example
```kotlin
/**
 * Performs Axon device detection based on MAC address and name.
 *
 * @param macAddress The MAC address to check
 * @param deviceName The device name (optional)
 * @param rssi Signal strength in dBm
 * @return True if device matches Axon signatures
 */
fun isAxonDevice(
    macAddress: String,
    deviceName: String?,
    rssi: Int
): Boolean {
    // Implementation
}
```

### Jetpack Compose

- Use **Material Design 3** components
- Follow **single responsibility** for composables
- Use **remember** for state that should persist
- Use **LaunchedEffect** for side effects

### Example
```kotlin
@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val colors = BlueMeanieTheme.colors
    
    GlassCard(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = colors.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = colors.textTertiary
            )
        }
    }
}
```

### Git

- Use **imperative mood** in commit messages (add, not added)
- Reference issues in commits when applicable
- Keep commits **focused and atomic**

---

## Questions?

If you have questions or need help, please:
- Open an issue for bugs
- Start a discussion for questions
- Check existing documentation

---

Thank you for contributing to BlueMeanie! 🎯