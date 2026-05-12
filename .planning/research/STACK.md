# Stack Research — JavaFX TextTwist

**Project:** CS120 Lab06 — TextTwist word game
**Researched:** 2026-05-12
**Research mode:** Ecosystem (spec-constrained — most choices are mandated)

---

## Recommended Stack

| Component | Choice | Version | Rationale |
|-----------|--------|---------|-----------|
| Language | Java | 21 (LTS) | Mandated by spec; matches JavaFX 21 module system |
| UI framework | JavaFX | 21 | Mandated by spec |
| Build tool | Maven | 3.9.x | Mandated by spec (openjfx fxml archetype) |
| JavaFX Maven plugin | javafx-maven-plugin (openjfx) | 0.0.8 | Current stable for JavaFX 21; handles `--add-modules` transparently |
| Project archetype | javafx-archetype-fxml | 0.0.6 | Spec mandates openjfx fxml archetype; generates FXML + controller scaffold |
| Testing | JUnit | 4.12 | Mandated by spec explicitly |
| Test runner (Maven) | maven-surefire-plugin | 2.22.2 | Minimum version with JUnit 4 platform support; do NOT use 3.x with JUnit 4.12 |
| Styling | External CSS file | — | Mandated by spec ("impeccable style"); loaded via `getClass().getResource()` |
| Dictionary I/O | Java standard library (Scanner / BufferedReader) | — | No external dep needed; files are plain-text line-by-line |

---

## pom.xml Key Config

### 1. Project coordinates and Java version

```xml
<groupId>cs120</groupId>
<artifactId>lab06</artifactId>
<version>1.0-SNAPSHOT</version>
<packaging>jar</packaging>

<properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
    <javafx.version>21</javafx.version>
</properties>
```

Rationale: `<maven.compiler.release>21</maven.compiler.release>` is cleaner than separate source/target in Java 9+ but both work. Use whatever the archetype generates.

---

### 2. JavaFX dependencies

```xml
<dependencies>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>${javafx.version}</version>
    </dependency>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-fxml</artifactId>
        <version>${javafx.version}</version>
    </dependency>

    <!-- JUnit 4.12 — mandated by spec -->
    <dependency>
        <groupId>junit</groupId>
        <artifactId>junit</artifactId>
        <version>4.12</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

Notes:
- `javafx-controls` is required for Button, Label, ListView, etc.
- `javafx-fxml` is required for FXMLLoader.
- Do NOT add `javafx-media` or `javafx-web` unless you use them — each bloats the classpath and may trigger module-info issues.
- The `org.openjfx` group ID is correct for all JavaFX 21 artifacts on Maven Central.

---

### 3. javafx-maven-plugin (build / run)

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-maven-plugin</artifactId>
            <version>0.0.8</version>
            <executions>
                <execution>
                    <!-- Default configuration for running with: mvn javafx:run -->
                    <id>default-cli</id>
                    <configuration>
                        <mainClass>cs120.lab06/cs120.lab06.App</mainClass>
                        <launcher>app</launcher>
                        <jlinkZipName>app</jlinkZipName>
                        <jlinkImageName>app</jlinkImageName>
                        <noManPages>true</noManPages>
                        <stripDebug>true</stripDebug>
                        <noHeaderFiles>true</noHeaderFiles>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

The `<mainClass>` format for a modular project is `module.name/fully.qualified.ClassName`. If you are NOT using `module-info.java`, drop the `module.name/` prefix (see module-info section below).

To run: `mvn javafx:run`

---

### 4. maven-surefire-plugin for JUnit 4.12

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>2.22.2</version>
    <!-- No extra config needed for JUnit 4 with this version -->
</plugin>
```

CRITICAL: Do not use surefire 3.x with JUnit 4.12 without additional configuration. Surefire 3.x defaults to the JUnit Platform provider which requires JUnit 5. With JUnit 4.12, stick to 2.22.2 which uses the JUnit 4 runner natively.

To run tests: `mvn test`

---

### 5. Complete minimal pom.xml (what the archetype generates, annotated)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
             https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>cs120</groupId>
    <artifactId>lab06</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <name>lab06</name>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <javafx.version>21</javafx.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-controls</artifactId>
            <version>${javafx.version}</version>
        </dependency>
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-fxml</artifactId>
            <version>${javafx.version}</version>
        </dependency>

        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.12</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.openjfx</groupId>
                <artifactId>javafx-maven-plugin</artifactId>
                <version>0.0.8</version>
                <executions>
                    <execution>
                        <id>default-cli</id>
                        <configuration>
                            <!-- Adjust to your actual main class -->
                            <mainClass>cs120.lab06/cs120.lab06.App</mainClass>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>2.22.2</version>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## module-info.java

The openjfx fxml archetype generates a `module-info.java`. For this assignment, it should look like:

```java
module cs120.lab06 {
    requires javafx.controls;
    requires javafx.fxml;

    // Opens the controller package to javafx.fxml so FXMLLoader can
    // inject @FXML fields via reflection
    opens cs120.lab06 to javafx.fxml;

    // Exports the package so JavaFX can instantiate Application
    exports cs120.lab06;
}
```

**GOTCHA — JUnit 4 + module-info:** JUnit 4 does not play nicely with the Java module system. The test runner needs reflective access to your test classes. Surefire 2.22.2 handles this by forking a JVM without module enforcement for tests. If you see `InaccessibleObjectException` during `mvn test`, add this to the surefire config:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>2.22.2</version>
    <configuration>
        <argLine>
            --add-opens cs120.lab06/cs120.lab06=ALL-UNNAMED
        </argLine>
    </configuration>
</plugin>
```

Alternatively — and this is the simpler path for a class assignment — delete `module-info.java` and run the project as an unnamed module. The javafx-maven-plugin will still inject `--add-modules javafx.controls,javafx.fxml` automatically. This avoids all module boundary friction with JUnit 4. The trade-off is no module encapsulation, which is fine for a lab assignment.

**Recommendation for CS120 Lab06:** Delete `module-info.java`. Run as unnamed module. Zero friction with JUnit 4.

---

## External CSS in JavaFX

The spec requires an external CSS file. The standard pattern:

```java
// In Application.start() or after loading FXML:
Scene scene = new Scene(root);
scene.getStylesheets().add(
    getClass().getResource("/css/styles.css").toExternalForm()
);
```

File location: `src/main/resources/css/styles.css`

Maven copies everything in `src/main/resources/` to the classpath root at build time, so `/css/styles.css` resolves correctly at runtime.

Do NOT use a hardcoded filesystem path like `new File("src/main/resources/css/styles.css").toURI()` — this breaks when run via `mvn javafx:run` vs. running the jar vs. running from an IDE.

**SceneBuilder integration:** SceneBuilder can open FXML files and apply CSS previews. Point SceneBuilder at your FXML file; it will pick up the stylesheet if the `<stylesheets>` element is declared in the FXML root element:

```xml
<!-- In twist.fxml -->
<BorderPane xmlns="http://javafx.com/javafx/21"
            xmlns:fx="http://javafx.com/fxml/1"
            fx:controller="cs120.lab06.TwistController"
            stylesheets="@../css/styles.css">
```

The `@` prefix in FXML means "relative to this FXML file". SceneBuilder resolves it; so does FXMLLoader at runtime.

---

## Dictionary File Access (twister_words/ at project root)

The spec places `twister_words/` at the **project root**, not inside `src/`. This means the files are NOT on the classpath and cannot be loaded with `getClass().getResource()`.

Use a path relative to the working directory instead:

```java
// WordDictionary constructor or loader:
Path dictPath = Path.of("twister_words", n + ".txt");
// n is the word length (3–10)
try (Scanner sc = new Scanner(dictPath)) {
    int count = Integer.parseInt(sc.nextLine().trim());
    // ... read count lines
}
```

When Maven runs (`mvn javafx:run`), the working directory is the project root (where `pom.xml` lives). This is the same directory that contains `twister_words/`, so `Path.of("twister_words", "3.txt")` resolves correctly.

When running from an IDE (IntelliJ, Eclipse, VS Code), the working directory is also typically the project root by default. Verify this in your run configuration if you see FileNotFoundException.

**Do NOT copy the dictionary into src/main/resources.** The spec is explicit that it lives at the project root. Copying it would create two sources of truth and contradict the spec.

---

## FXML + MVC Pattern

The archetype generates:
- `src/main/resources/cs120/lab06/primary.fxml` — rename to `twist.fxml`
- `src/main/java/cs120/lab06/PrimaryController.java` — rename to `TwistController.java`
- `src/main/java/cs120/lab06/App.java` — Application entry point

Standard MVC wiring:

```
Model:      WordDictionary.java, TwistController.java (game state)
View:       twist.fxml + styles.css
Controller: TwistController.java (JavaFX @FXML controller)
```

Note: In JavaFX MVC, the "controller" class in the FXML sense (TwistController) plays double duty as both the MVC controller AND holds game model state per the spec's UML. This is intentional — the spec's TwistController holds `score`, `levelScore`, `level`, `targetWord`, `myDictionary` as fields.

FXMLLoader wiring in App.java:

```java
FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("twist.fxml"));
Scene scene = new Scene(fxmlLoader.load(), 800, 600);
scene.getStylesheets().add(
    App.class.getResource("/css/styles.css").toExternalForm()
);
primaryStage.setScene(scene);
primaryStage.show();
```

---

## Project Structure

```
lab06/                              ← project root (mvn working dir)
├── pom.xml
├── twister_words/                  ← dictionary files (NOT in src/)
│   ├── 3.txt
│   ├── 4.txt
│   ├── 5.txt
│   ├── 6.txt
│   ├── 7.txt
│   ├── 8.txt
│   ├── 9.txt
│   └── 10.txt
└── src/
    ├── main/
    │   ├── java/
    │   │   └── cs120/
    │   │       └── lab06/
    │   │           ├── App.java              ← Application entry point
    │   │           ├── WordDictionary.java   ← Model: dictionary + word lookup
    │   │           └── TwistController.java  ← Model + FXML controller
    │   └── resources/
    │       ├── css/
    │       │   └── styles.css               ← External CSS (spec requirement)
    │       └── cs120/
    │           └── lab06/
    │               └── twist.fxml           ← FXML view layout
    └── test/
        └── java/
            └── cs120/
                └── lab06/
                    └── WordDictionaryTest.java  ← JUnit 4.12 tests
```

Notes on the FXML resource path: The archetype places FXML files under `src/main/resources/cs120/lab06/` (matching the package). `App.class.getResource("twist.fxml")` resolves to this location correctly because the class and the resource share the same package path on the classpath.

---

## macOS-Specific Gotchas

### 1. JavaFX must run on the main thread (macOS AppKit requirement)

JavaFX Applications MUST be launched from the main thread on macOS. Do not call `Platform.startup()` or `Application.launch()` from a non-main thread. The archetype generates correct code; do not refactor it naively.

### 2. OpenJDK + JavaFX on macOS: use a JDK that bundles JavaFX OR rely on Maven

If you install a JDK that does NOT bundle JavaFX (most OpenJDK distributions: Temurin, Azul Zulu, etc.), the Maven dependencies provide the JavaFX native libraries automatically via platform classifiers. Maven resolves the correct native `.dylib` for macOS (arm64 for Apple Silicon, x86_64 for Intel). You do NOT need to install JavaFX separately if using Maven.

If running `java -jar` directly (not via Maven), you must either use a JDK with JavaFX bundled (BellSoft Liberica, Azul Zulu FX builds) or specify `--module-path` and `--add-modules` manually. For this assignment, always run via `mvn javafx:run` to avoid this complexity.

### 3. Apple Silicon (arm64)

JavaFX 21 has native ARM64 support for Apple Silicon (M1/M2/M3). Maven automatically downloads the correct native artifact. No special configuration needed.

### 4. macOS Ventura/Sonoma: no JavaFX security prompts

JavaFX 21 desktop apps do not require special macOS entitlements for a sandboxed educational assignment. You may get a first-run Gatekeeper warning if you distribute a signed jar, but `mvn javafx:run` runs locally and is unaffected.

---

## Version Conflict Matrix

| Combination | Status | Notes |
|-------------|--------|-------|
| JavaFX 21 + JDK 21 | SAFE | Perfect match; LTS pair |
| JavaFX 21 + JDK 17 | UNSAFE | JavaFX 21 modules may reference JDK 21 APIs |
| JavaFX 21 + JDK 11 | UNSAFE | Module system differences; do not use |
| javafx-maven-plugin 0.0.8 + JavaFX 21 | SAFE | Tested combination |
| javafx-maven-plugin 0.0.6 + JavaFX 21 | LIKELY SAFE | Plugin is additive; older versions generally work |
| surefire 2.22.2 + JUnit 4.12 | SAFE | Canonical pairing |
| surefire 3.x + JUnit 4.12 | RISKY | Needs explicit JUnit4 provider config; avoid |
| module-info.java + JUnit 4.12 | FRICTION | JUnit 4 not module-aware; recommend dropping module-info |

---

## Archetype Generation Command (for reference)

```bash
mvn archetype:generate \
  -DarchetypeGroupId=org.openjfx \
  -DarchetypeArtifactId=javafx-archetype-fxml \
  -DarchetypeVersion=0.0.6 \
  -DgroupId=cs120 \
  -DartifactId=lab06 \
  -Dversion=1.0-SNAPSHOT \
  -Djavafx-version=21 \
  -DinteractiveMode=false
```

If the archetype version 0.0.6 is not found in central, try 0.0.3 (older but widely cached). The archetype version controls the scaffold, not the JavaFX version — the generated pom.xml's `<javafx.version>` property is what matters.

---

## Confidence Notes

| Recommendation | Confidence | Basis |
|----------------|------------|-------|
| `org.openjfx` group ID and artifact IDs | HIGH | Stable since JavaFX 11; unchanged through JavaFX 21 |
| javafx-maven-plugin 0.0.8 | MEDIUM | Correct as of mid-2024; verify on Maven Central that 0.0.8 is the latest before using |
| JUnit 4.12 + surefire 2.22.2 | HIGH | Long-stable pairing; JUnit 4.12 itself is from 2014 |
| Drop module-info.java for JUnit 4 compat | HIGH | Well-documented friction point; unnamed module path is the standard workaround |
| twister_words/ path resolution via working dir | HIGH | Maven sets working dir to project root; matches spec requirement |
| CSS via getClass().getResource() | HIGH | JavaFX standard pattern; classpath-relative loading |
| macOS ARM64 native JavaFX via Maven | HIGH | OpenJFX has published arm64 artifacts since JavaFX 17 |
| surefire 3.x incompatibility with JUnit 4 | MEDIUM | Confirmed pattern as of 2023-2024; surefire 3.x defaults changed |
| Archetype version 0.0.6 | MEDIUM | Used in official openjfx docs as of 2023; may be superseded |

**Overall confidence: HIGH** for the core configuration. The mandated stack (JavaFX 21, Maven, JUnit 4.12) is mature and well-documented. The only low-friction risk is the module-info.java + JUnit 4 interaction, which is resolved by omitting module-info.

---

## Sources

- OpenJFX official documentation: https://openjfx.io/openjfx-docs/ (training knowledge, JavaFX 11–21 era)
- OpenJFX Maven archetypes: https://github.com/openjfx/javafx-maven-archetypes
- javafx-maven-plugin: https://github.com/openjfx/javafx-maven-plugin
- Maven Surefire Plugin docs: https://maven.apache.org/surefire/maven-surefire-plugin/
- JUnit 4 on Maven Central: https://mvnrepository.com/artifact/junit/junit/4.12
- Note: Network tools were unavailable during this research session. All findings are from training knowledge (cutoff August 2025). The JavaFX 21 + Maven stack has been stable since 2023; HIGH confidence findings reflect this stability. MEDIUM confidence findings should be verified against Maven Central for current plugin versions before project creation.
