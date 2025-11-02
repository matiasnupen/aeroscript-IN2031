# AeroScript

A domain-specific language for programming autonomous drone missions.

## How to Run

1. Build the project:
   ```bash
   ./gradlew shadowJar
   ```

2. Run with a mission file:
   ```bash
   java -jar build/libs/aeroscript-1.0-all.jar src/test/resources/simpleTest.aero
   ```

3. Use REPL commands like `help`, `messages`, `message launch`, `battery`, `position`, `exit`