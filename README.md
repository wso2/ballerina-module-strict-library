# ballerina-module-strict-library

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

A Ballerina compiler plugin that enforces library project constraints at compile time. It ensures library packages do not contain executable entrypoints such as `public function main()`, `function init()`, service declarations, or listener declarations.

## How It Works

When a Ballerina package imports `wso2/strict.library` in any `.bal` file, this compiler plugin activates during compilation and scans all source files in the default module. If any entrypoints are found, the plugin reports a compile-time error with diagnostic code `STRICT_LIBRARY_101`. Multiple violations are reported in a single error message.

## Project Structure

| Directory | Description |
|-----------|-------------|
| `compiler-plugin/` | Java implementation of the compiler plugin |
| `compiler-plugin-tests/` | Integration tests with sample Ballerina projects |
| `ballerina/` | Ballerina marker package that library projects import |
| `build-config/` | Checkstyle and code quality configuration |

## Prerequisites

- JDK 17+
- Ballerina 2201.13.0
- Gradle 8.11.1 (included via wrapper)
- GitHub Packages authentication for Ballerina platform dependencies:
  - `packageUser` — GitHub username
  - `packagePAT` — GitHub personal access token

## Building from Source

```bash
# Build the compiler plugin
./gradlew build

# Run integration tests
./gradlew :strict-library-compiler-plugin-tests:test

# Run code quality checks
./gradlew :strict-library-compiler-plugin:checkstyleMain
./gradlew :strict-library-compiler-plugin:spotbugsMain
```

## Usage

Add the following import to any `.bal` file in your library project:

```ballerina
import wso2/strict.library as _;
```

The compiler plugin will automatically validate that your project does not contain:

| Construct | Example |
|-----------|---------|
| Public main function | `public function main() { }` |
| Module init function | `function init() { }` |
| Service declaration | `service / on ep { }` |
| Listener declaration | `listener http:Listener ep = new(8080);` |

If any of these are found, compilation fails with a `STRICT_LIBRARY_101` error:

```
error [STRICT_LIBRARY_101]: library projects must not contain entrypoints. Found: 'public function main' in main.bal
```

Multiple violations are combined into a single diagnostic, e.g.:

```
error [STRICT_LIBRARY_101]: library projects must not contain entrypoints. Found: 'public function main' in main.bal, service declaration in svc.bal
```

## Contributing

Contributions are welcome. Before submitting a pull request, ensure all checks pass:

```bash
./gradlew build && ./gradlew :strict-library-compiler-plugin-tests:test
```

This project enforces:
- **Checkstyle** for code style
- **SpotBugs** for static bug detection
- **TestNG** for testing

## License

This project is licensed under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).
