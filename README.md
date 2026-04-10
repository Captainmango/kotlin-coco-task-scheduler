# kotlin-coco-bingo

A Kotlin implementation of a cron expression parser and task scheduler.

## Features

- Parse standard cron expressions with 5 fields (minute, hour, day of month, month, day of week)
- Supports wildcards (`*`), lists (`1,15,30`), ranges (`1-5`), step values (`*/15`), and singular values
- CLI for task scheduling
- Web application for managing scheduled tasks (planned)

## Project Structure

- `core/` - Core library containing the cron parser and domain models
- `cli/` - Command-line interface for scheduling tasks

## Requirements

- JDK 21 or higher
- Gradle (wrapper included)

## Building

This project uses Gradle for build management:

```bash
# Build the entire project
./gradlew build

# Build the CLI fat JAR
./gradlew :cli:fatJar
```

## Running the CLI

### Using Gradle

```bash
# Run the CLI directly via Gradle
./gradlew :cli:run

# Run with arguments
./gradlew :cli:run --args="<your-args-here>"
```

### Using the Fat JAR

```bash
# Build the fat JAR first
./gradlew :cli:fatJar

# Run the JAR
java -jar cli/build/libs/cli.jar
```

## Running Tests

```bash
./gradlew test
```
