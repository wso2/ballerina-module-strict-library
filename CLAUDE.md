# module-strict-library

## Project Overview

A Ballerina compiler plugin that validates library packages at compile time. It ensures library projects do not contain executable entrypoints (`public function main`, `function init`, service declarations, or listener declarations). When a Ballerina package imports `wso2/strict.library` in **any** `.bal` file, this plugin activates and reports errors if any entrypoints are found.

## Tech Stack

- **Language**: Java (compiled with Java 9+ module system)
- **Target Platform**: Ballerina 2201.13.0 compiler plugin API
- **Build System**: Gradle 8.11.1 (wrapper included)
- **Testing**: TestNG 7.6.1
- **Code Quality**: Checkstyle 10.12.0, SpotBugs 6.0.18
- **License**: Apache 2.0 (WSO2 LLC)

## Project Structure

```
├── compiler-plugin/          # Main plugin implementation (Java)
├── compiler-plugin-tests/    # Integration tests
├── ballerina/                # Ballerina marker package (imported by library projects)
├── build-config/checkstyle/  # Checkstyle rule downloads
└── gradle/                   # Gradle wrapper
```

### compiler-plugin/ — Core Implementation

All classes under `io.wso2.strict.library`:

| Class | Purpose |
|-------|---------|
| `StrictLibraryCompilerPlugin` | Entry point; extends `CompilerPlugin`, registers the analyzer |
| `StrictLibraryProjectAnalyzer` | Extends `CodeAnalyzer`; registers the same validator instance for `FUNCTION_DEFINITION`, `SERVICE_DECLARATION`, and `LISTENER_DECLARATION` syntax kinds |
| `StrictLibraryProjectValidator` | Core validation logic; implements `AnalysisTask<SyntaxNodeAnalysisContext>`; uses `AtomicBoolean` so the full scan runs only once regardless of how many syntax kinds triggered it |
| `StrictLibraryDiagnostic` | Enum-based diagnostic registry with code, message template, and severity |
| `Constants` | String constants: `WSO2`, `STRICT`, `LIBRARY`, `MAIN`, `INIT`, `PUBLIC_KEYWORD` |

**Validation flow (`StrictLibraryProjectValidator.perform`):**
1. Guard with `AtomicBoolean` so the scan only runs once per compilation.
2. Iterate **all modules** in the package and scan their documents for the `wso2/strict.library` import.
3. If the import is not found anywhere, return early (plugin inactive).
4. If found, scan **all modules** for entrypoints and collect their descriptions.
5. If any entrypoints are found, report a single `STRICT_LIBRARY_101` diagnostic at the import's location, listing all violations.

### compiler-plugin-tests/ — Integration Tests

- Single test class: `CompilerPluginTest.java` with 10 scenario-based tests
- Each test scenario is a complete Ballerina project under `src/test/resources/ballerina_sources/`
- Tests compile real Ballerina projects using the Ballerina distribution and assert on diagnostic output

| Test | Scenario |
|------|----------|
| `testValidLibraryProject` | No entrypoints — expects zero `STRICT_LIBRARY_*` errors |
| `testProjectWithMain` | Has `public function main` — expects 1 error |
| `testProjectWithInit` | Has `function init` — expects 1 error |
| `testProjectWithService` | Has service declaration — expects 1 error |
| `testProjectWithListener` | Has listener declaration — expects 1 error |
| `testProjectWithMultipleEntrypoints` | Has both `main` and a service — expects 1 error listing both |
| `testProjectWithoutLibBal` | Import is in `other.bal` (no `lib.bal`) — plugin still activates, expects 1 error |
| `testProjectImportNotInLib` | Import is in `other.bal`, lib.bal has no import — plugin still activates, expects 1 error |
| `testProjectImportInSubmodule` | Import is in a sub-module file, `function init` in that sub-module — plugin activates and detects cross-module entrypoint |
| `testProjectServiceInSubmodule` | Import is in the default module, service declaration is in a sub-module — plugin detects entrypoint across module boundary |

### ballerina/ — Marker Package

- `Ballerina.toml` — package manifest (org: wso2, name: strict.library)
- `CompilerPlugin.toml` — declares plugin class and JAR dependency
- `strict_library.bal` — empty marker module that library projects import

## Build & Test Commands

```bash
# Build the compiler plugin
./gradlew build

# Run tests (automatically sets up Ballerina distribution and bala structure)
./gradlew :strict-library-compiler-plugin-tests:test

# Code quality checks
./gradlew :strict-library-compiler-plugin:checkstyleMain
./gradlew :strict-library-compiler-plugin:spotbugsMain
```

### Gradle Modules (settings.gradle)

| Gradle Module | Directory |
|---------------|-----------|
| `:strict-library-compiler-plugin` | `compiler-plugin/` |
| `:strict-library-compiler-plugin-tests` | `compiler-plugin-tests/` |
| `:checkstyle` | `build-config/checkstyle/` |

### Environment Variables

GitHub Packages authentication (for Ballerina dependencies):
- `packageUser` — GitHub username
- `packagePAT` — GitHub personal access token

### Version Management

All versions centralized in `gradle.properties`. Key property: `ballerinaLangVersion=2201.13.0` drives all Ballerina dependency versions. The `ballerina/Ballerina.toml` version is kept in sync manually.

## Key Conventions

- Diagnostic codes follow the pattern `STRICT_LIBRARY_NNN` (currently only `STRICT_LIBRARY_101`)
- Plugin activation is conditional: validates only when `wso2/strict.library` is imported in **any** `.bal` file across **any module** of the package (default or sub-modules)
- Validator uses `AtomicBoolean` to ensure a single full-module scan across multiple syntax kind triggers
- The single diagnostic is reported at the location of the `wso2/strict.library` import declaration
- Tests are integration-level: they load and compile full Ballerina projects, not unit tests of individual methods

## Adding a New Validation Rule

1. Add detection logic in `StrictLibraryProjectValidator.getEntrypointDescription()` (`compiler-plugin/src/main/java/io/wso2/strict/library/StrictLibraryProjectValidator.java`)
2. If a new diagnostic code is needed, add an enum constant in `StrictLibraryDiagnostic`
3. If a new syntax kind needs to trigger the scan, register it in `StrictLibraryProjectAnalyzer.init()`
4. Add a test project under `compiler-plugin-tests/src/test/resources/ballerina_sources/`
5. Add a test method in `CompilerPluginTest`

## Additional Documentation

| Topic | File |
|-------|------|
| Architectural patterns and design decisions | [.claude/docs/architectural_patterns.md](.claude/docs/architectural_patterns.md) |
