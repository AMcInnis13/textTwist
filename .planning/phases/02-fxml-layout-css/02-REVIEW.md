---
phase: 02-fxml-layout-css
reviewed: 2026-05-12T00:00:00Z
depth: standard
files_reviewed: 4
files_reviewed_list:
  - src/main/resources/cs120/lab06/styles.css
  - src/main/resources/cs120/lab06/primary.fxml
  - src/main/java/cs120/lab06/PrimaryController.java
  - src/main/java/cs120/lab06/App.java
findings:
  critical: 2
  warning: 4
  info: 3
  total: 9
status: issues_found
---

# Phase 02: Code Review Report

**Reviewed:** 2026-05-12
**Depth:** standard
**Files Reviewed:** 4
**Status:** issues_found

## Summary

Four files were reviewed: the external CSS stylesheet, the FXML layout, the JavaFX controller stub, and the application entry point. The controller stub (`PrimaryController.java`) is clean for its Phase 2 scope. The CSS file follows the `-fx-` prefix convention correctly and contains all required named classes. The FXML is structurally sound but has two defects that will cause silent runtime failures. `App.java` has one crash risk under a packaging error.

The two blockers are: a missing CSS class definition that leaves the left panel unstyled and orphaned in the contract, and a FXML/CSS value conflict on `letterButtonArea`'s `minHeight` where CSS silently overrides the FXML attribute to a smaller value. Together these indicate the layout was not fully cross-checked between the two files.

---

## Critical Issues

### CR-01: `.found-words-panel` CSS class referenced in FXML but never defined in stylesheet

**File:** `src/main/resources/cs120/lab06/primary.fxml:19`
**Issue:** The left-panel VBox applies `styleClass="found-words-panel"`, but `styles.css` contains no `.found-words-panel` selector. JavaFX silently ignores unresolved CSS class names, so the left VBox receives no background, border, padding, or sizing from CSS. Phase 3 code that attempts to toggle or augment `.found-words-panel` will also silently fail. The project spec requires "impeccable style" and grades CSS — a panel with no applied CSS class is a visible grading gap.

**Fix:** Add the missing class to `styles.css`. Minimal definition to match the dark theme:

```css
/* Section 2b: Found-words panel container (left VBox) */
.found-words-panel {
    -fx-background-color: #16213e;
    -fx-padding: 0 0 8 0;
}
```

---

### CR-02: `minHeight` conflict between FXML attribute and CSS rule on `letterButtonArea`

**File:** `src/main/resources/cs120/lab06/primary.fxml:54` and `src/main/resources/cs120/lab06/styles.css:93`
**Issue:** The FXML sets `minHeight="70"` on the `FlowPane`, but the `.letter-area` CSS rule sets `-fx-min-height: 60`. In JavaFX, CSS takes priority over values set by FXML attributes (CSS overrides the Java setter equivalent). The effective minimum height will be **60 px**, not 70 px. The FXML value is silently ignored. This is a latent layout bug: Phase 3 will add up to 10 letter buttons (10-letter words at Level 8), and the 60 px floor may clip the FlowPane when two rows of buttons appear.

**Fix:** Pick one authoritative source. The CSS is authoritative per project rules, so remove the conflicting attribute from FXML:

```xml
<!-- Before -->
<FlowPane fx:id="letterButtonArea" styleClass="letter-area"
          hgap="8" vgap="8"
          prefWrapLength="400"
          alignment="CENTER"
          minHeight="70" />

<!-- After: remove minHeight; CSS .letter-area -fx-min-height controls it.
     Also raise the CSS value to accommodate two button rows. -->
<FlowPane fx:id="letterButtonArea" styleClass="letter-area"
          hgap="8" vgap="8"
          prefWrapLength="400"
          alignment="CENTER" />
```

And update the CSS `-fx-min-height` to the intended value:

```css
.letter-area {
    ...
    -fx-min-height: 70;   /* was 60; must fit two rows of letter-btn at Level 8 */
}
```

---

## Warnings

### WR-01: `App.class.getResource()` result used without null check — NPE on resource-not-found

**File:** `src/main/java/cs120/lab06/App.java:21`
**Issue:** `App.class.getResource("styles.css")` returns `null` if the resource is absent from the classpath (e.g., a Maven build that omits resources, or a path typo after renaming). Calling `.toExternalForm()` on `null` throws a `NullPointerException` with no diagnostic text — the app silently crashes at startup with no indication of which resource is missing.

**Fix:**

```java
java.net.URL cssUrl = App.class.getResource("styles.css");
if (cssUrl == null) {
    throw new IllegalStateException(
        "styles.css not found on classpath alongside App.class");
}
scene.getStylesheets().add(cssUrl.toExternalForm());
```

---

### WR-02: Duplicate spacing values set in both FXML attributes and CSS — CSS wins silently

**File:** `src/main/resources/cs120/lab06/primary.fxml:35,45,68` and `src/main/resources/cs120/lab06/styles.css:212,217,222`
**Issue:** Three containers set `spacing` both as a FXML attribute and via a CSS class property:
- `<VBox spacing="12" styleClass="center-vbox">` — `.center-vbox` CSS also sets `-fx-spacing: 12`
- `<HBox spacing="20" styleClass="info-hbox">` — `.info-hbox` CSS also sets `-fx-spacing: 20`
- `<HBox spacing="8"  styleClass="control-hbox">` — `.control-hbox` CSS also sets `-fx-spacing: 8`

Currently the values agree so there is no visual bug, but CSS always silently overrides FXML attribute values. A future maintainer changing only the CSS value will not notice the stale FXML attribute, and vice versa — two out-of-sync sources of truth. This pattern already caused CR-02 (`minHeight`).

**Fix:** Remove the inline `spacing` attributes from FXML for all three containers; let CSS be the single source of truth per project rules:

```xml
<VBox styleClass="center-vbox">
<HBox styleClass="info-hbox" alignment="CENTER_LEFT">
<HBox styleClass="control-hbox" alignment="CENTER">
```

---

### WR-03: `.root` applies `-fx-text-fill` to a non-Labeled node — property silently ignored

**File:** `src/main/resources/cs120/lab06/styles.css:17`
**Issue:** The `.root` rule sets `-fx-text-fill: #e0e0e0`. The root node is a `BorderPane`, which is a `Pane` (not a `Labeled`). `BorderPane` does not support the `-fx-text-fill` CSS property; the declaration is silently discarded by JavaFX. Labels do not inherit `-fx-text-fill` from an ancestor pane — each `Label` needs its own text fill rule. The property does nothing here and creates a false impression that it propagates to child labels.

**Fix:** Remove the dead declaration from `.root`:

```css
.root {
    -fx-background-color: #1a1a2e;
    -fx-font-family: "System";
    /* Remove: -fx-text-fill: #e0e0e0;  -- BorderPane ignores this property */
}
```

Ensure all Labels that need `#e0e0e0` text have their own CSS class (`.info-label`, `.section-header`, etc.), which they already do.

---

### WR-04: `setRoot()` is dead code in Phase 2 and exposes a NullPointerException path

**File:** `src/main/java/cs120/lab06/App.java:27-29`
**Issue:** `static void setRoot(String fxml)` is package-private and never called from any file in the current codebase. More critically, it calls `scene.setRoot(...)` where `scene` is the static field initialized in `start()`. If `setRoot()` is ever called before `start()` completes (or from a test), `scene` is `null` and a NPE results with no diagnostic. The method is also not part of the Phase 2 or Phase 3 plan — it appears to be a leftover from the Maven archetype template.

**Fix:** If this method is not needed in any planned phase, delete it. If it is needed, add a null guard:

```java
static void setRoot(String fxml) throws IOException {
    if (scene == null) {
        throw new IllegalStateException("setRoot() called before start()");
    }
    scene.setRoot(loadFXML(fxml));
}
```

---

## Info

### IN-01: `minWidth` attribute on `guessDisplay` HBox is redundant with CSS

**File:** `src/main/resources/cs120/lab06/primary.fxml:61-65`
**Issue:** `<HBox ... minWidth="200">` and `.guess-display` CSS both set `-fx-min-width: 200`. These agree so there is no current bug, but this is the same dual-source pattern that produced CR-02 and WR-02. Low risk because values match, but worth cleaning up for consistency.

**Fix:** Remove `minWidth="200"` from the FXML element; the CSS class owns it.

---

### IN-02: `timeLabel` initial text `"2:00"` does not match the 120-second timer spec

**File:** `src/main/resources/cs120/lab06/primary.fxml:49`
**Issue:** The label displays `"2:00"` at startup, which is correct (120 seconds = 2:00). However, once Phase 3 wires the `Timeline`, the label will be overwritten immediately. The hardcoded text is cosmetically acceptable but creates the impression the timer is static during Phase 2 manual inspection. Not a functional bug — noting it for Phase 3 awareness.

**Fix:** No action required; Phase 3 will overwrite this on timer initialization.

---

### IN-03: `lastWordBtn` `disable="true"` set in FXML — redundant once Phase 3 manages state

**File:** `src/main/resources/cs120/lab06/primary.fxml:72`
**Issue:** The "Last Word" button is disabled in FXML via `disable="true"`. This is correct for initial state. Phase 3 must programmatically enable/disable this button based on game state. The FXML attribute only sets the initial state and will be overridden — this is fine, but the team should be aware the FXML attribute has no persistent effect once the controller manages it.

**Fix:** No change needed in Phase 2. Phase 3 should document that it owns the `lastWordBtn.setDisable()` contract.

---

_Reviewed: 2026-05-12_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
