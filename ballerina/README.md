# Ballerina Strict Library

[![Build](https://github.com/wso2/ballerina-module-strict-library/actions/workflows/build-timestamped-master.yml/badge.svg)](https://github.com/wso2/ballerina-module-strict-library/actions/workflows/build-timestamped-master.yml)
[![GitHub Last Commit](https://img.shields.io/github/last-commit/wso2/ballerina-module-strict-library.svg)](https://github.com/wso2/ballerina-module-strict-library/commits/main)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

The `wso2/strict.library` package provides compile-time validation for Ballerina library projects. It ensures that library packages do not contain executable entrypoints, enforcing a clean separation between reusable library code and application logic.

## Usage

Add the import to any `.bal` file in your library project. The conventional place is a dedicated `lib.bal`:

```ballerina
import wso2/strict.library as _;
```

The compiler plugin activates automatically during compilation — no additional configuration is needed. Once active, it scans all source files in the default module for prohibited constructs.

## What Is Validated

The following constructs are not allowed in a library project:

| Construct | Example |
|-----------|---------|
| Public main function | `public function main() { }` |
| Module init function | `function init() { }` |
| Service declaration | `service / on ep { }` |
| Listener declaration | `listener http:Listener ep = new(8080);` |

If any are detected, compilation fails with diagnostic code `STRICT_LIBRARY_101`:

```
error [STRICT_LIBRARY_101]: library projects must not contain entrypoints. Found: 'public function main' in main.bal
```

Multiple violations are combined into a single error, listing each with the file where it was found.

## Why Use This

Library packages expose reusable functions, types, and classes for other Ballerina projects. Including entrypoints like `main`, services, or listeners in a library is typically a mistake and can cause unexpected behavior when the package is used as a dependency. This module catches those mistakes at compile time, before they can affect downstream consumers.

## Issues and Projects

The [Issues](https://github.com/wso2/ballerina-module-strict-library/issues) and [Projects](https://github.com/wso2/ballerina-module-strict-library/projects) tabs are used to track work on this module.

## Building from Source

```bash
git clone https://github.com/wso2/ballerina-module-strict-library.git
cd ballerina-module-strict-library
./gradlew build
./gradlew :strict-library-compiler-plugin-tests:test
```

## Contributing

Contributions are welcome. See the [contribution guidelines](https://github.com/ballerina-platform/ballerina-lang/blob/master/CONTRIBUTING.md) for more information.

## License

This project is licensed under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).
