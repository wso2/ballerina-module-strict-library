# Architectural Patterns and Design Decisions

## Plugin Activation via Marker Import

The plugin uses a **marker package pattern**: it only activates when the project under compilation imports `wso2/strict.library`. The plugin does not blindly validate every Ballerina project — it searches all documents in the default module for the import first, and returns early if not found.

This keeps the plugin opt-in and avoids interfering with non-library projects.

## Single Scan via AtomicBoolean

The Ballerina compiler plugin API calls `AnalysisTask.perform()` once per matched syntax node. Since `StrictLibraryProjectValidator` is registered for three syntax kinds (`FUNCTION_DEFINITION`, `SERVICE_DECLARATION`, `LISTENER_DECLARATION`), it could be called many times per compilation.

To avoid redundant full-module scans, the validator uses an `AtomicBoolean executed` flag via `compareAndSet(false, true)`. Only the first call performs the scan; all subsequent calls return immediately. This is thread-safe because the Ballerina compiler may invoke analysis tasks concurrently.

## Single Diagnostic for All Violations

Rather than reporting one diagnostic per entrypoint found, the plugin collects all violations in a list and emits a **single `STRICT_LIBRARY_101` diagnostic** listing all of them. The diagnostic is anchored at the location of the `wso2/strict.library` import declaration.

**Rationale:** A single comprehensive error is easier to act on than a flood of individual errors, especially when a developer accidentally adds many entrypoints at once. The error message format is:

```
library projects must not contain entrypoints. Found: 'public function main' in main.bal, service declaration in svc.bal
```

## Validator Scans All Module Documents

Even though the validator is triggered by a specific syntax node in one document, it iterates over **all documents** in the default module to find both the import and entrypoints. This ensures:
- The `wso2/strict.library` import can appear in any file (not constrained to `lib.bal`)
- All entrypoints across all files are discovered in a single pass

## Enum-Based Diagnostic Registry

`StrictLibraryDiagnostic` is an enum holding diagnostic code, message template, and severity. This pattern (common in WSO2 Ballerina plugins) makes it easy to add new diagnostics and keeps all diagnostic metadata in one place, rather than scattering string literals across the codebase.

## Integration Test Pattern

Tests use a pattern common in Ballerina compiler plugin development:
1. Each test scenario is a **complete Ballerina project** (with its own `Ballerina.toml`) under `src/test/resources/ballerina_sources/`
2. The test loads the project using `BuildProject.load()` against the extracted Ballerina distribution
3. It calls `currentPackage.getCompilation()` which triggers the compiler plugin
4. It then filters the `DiagnosticResult` for `STRICT_LIBRARY_*` error codes and asserts on count and message content

This approach tests the full compilation pipeline, not just individual methods in isolation.
