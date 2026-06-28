# ig-codegen

Generates Java constant classes from FHIR Implementation Guide (IG) packages, so canonical
`CodeSystem`/`StructureDefinition` (Profile)/`Extension` URLs don't have to be hand-transcribed
into application config — and silently go stale when the upstream IG renames or adds/removes a
resource.

## How it works

1. You restore the IG packages you depend on locally with the Firely Terminal CLI
   (`fhir restore`), which reads a package manifest's `dependencies` map and populates
   `~/.fhir/packages/<package-name>#<version>/package/*.json`.
2. `ig-codegen` reads that same manifest, scans each restored package's resource files, and
   classifies them:

   | Condition | Constant | Value |
   |---|---|---|
   | `resourceType == "CodeSystem"` | `CodeSystems` | `url` |
   | `StructureDefinition`, `kind == "complex-type"`, `derivation == "constraint"`, `type == "Extension"` | `Extensions` | `url` |
   | `StructureDefinition`, `kind == "resource"`, `derivation == "constraint"` | `Profiles` | `url + "|" + version` |

   `kind == "logical"` (logical models) and `derivation == "specialization"` (base type
   definitions) are skipped — they show up in real packages but aren't IG profiles.

   `hl7.fhir.r4.core` is skipped: it has no IG-specific canonical prefix of its own.

3. One Java class is generated per FHIR package, in a Java package with the **same name** as the
   FHIR package (e.g. FHIR package `de.medizininformatikinitiative.kerndatensatz.onkologie` →
   Java package `de.medizininformatikinitiative.kerndatensatz.onkologie`, class `Onkologie`),
   with one nested utility class per non-empty category (`CodeSystems` / `Profiles` /
   `Extensions`). Each canonical URL becomes a static no-arg accessor method, named in
   record-accessor style (lowerCamelCase, no `get` prefix), e.g. the FHIR id
   `mii-pr-diagnose-condition` becomes `miiPrDiagnoseCondition()`:

   ```java
   Onkologie.Profiles.miiPrDiagnoseCondition()
   // -> "https://www.medizininformatik-initiative.de/.../StructureDefinition/mii-pr-diagnose-condition|2026.0.0"
   ```

4. A CodeSystem that ships its own concepts inline (`content == "complete"`) additionally gets a
   nested enum, named after the CodeSystem, with one constant per concept and a `coding()`
   accessor returning a HAPI `org.hl7.fhir.r4.model.Coding`:

   ```java
   Onkologie.CodeSystems.MiiCsOnkoIntention.K.coding()
   // -> Coding{system=".../CodeSystem/mii-cs-onko-intention", code="K", display="kurativ"}
   ```

   External terminologies (SNOMED CT, LOINC, ICD-10, ...) ship with `content == "not-present"` —
   FHIR doesn't redistribute those, so there's nothing to expand; they keep just the URL accessor.

   Concept codes that aren't valid Java identifiers as-is are sanitized (`NameUtils.toEnumConstantName`):
   leading digits get a `_` prefix (`"2"` → `_2`), and codes ending in `+`/`-` (common for
   receptor-status codes like `mol+`/`mol-`) get a `_POS`/`_NEG` suffix instead of just stripping the
   sign — otherwise `"i+"` and `"i-"` would both sanitize to the same identifier. Any remaining
   collision within a CodeSystem is disambiguated with a numeric suffix (`_2`, `_3`, ...). Nested
   concept hierarchies are flattened (both group and leaf concepts become constants).

   Because the generated `coding()` accessor returns a HAPI type, depending on this generator's
   output pulls in `hapi-fhir-structures-r4` as a runtime dependency wherever any CodeSystem enum
   is generated.

## Usage

```java
List<Path> generatedFiles = new IgCodegen().generate(
    Path.of("package.json"),                              // your FHIR package manifest
    Path.of(System.getProperty("user.home"), ".fhir", "packages"), // Firely Terminal cache
    Path.of("src/main/java"));                             // where to write generated classes
```

Or run `IgCodegen.main(String[] args)` directly (`args[0]` = package.json, `args[1]` =
`~/.fhir/packages`, `args[2]` = output dir; all optional, with those same defaults).

### As a manually-invoked Gradle task

This module wires up `generateSampleIgConstants` (see `build.gradle`) as an example. Consumers
should add an equivalent `JavaExec` task in their own build, pointing `outputDir` at a **checked-in
source root**, and treat it as a "generate, review, commit" dev tool rather than a build-graph
participant:

- Don't write a custom `Task` type with `@OutputDirectory` pointing at that checked-in directory —
  Gradle's task validation will (correctly) flag an undeclared implicit dependency with
  `compileJava`, since the generated sources are meant to be committed, not regenerated on every
  build. Use `@Internal` instead if you do write a custom task.
- Mark the task `notCompatibleWithConfigurationCache(...)`: it reads from outside the project
  (`~/.fhir/packages`, `user.home`).
- If your build uses Spotless (or another formatter) with a project-wide `target`/`targetExclude`,
  make sure it doesn't recursively reformat the generated package.

## Diffing two IG package versions

Comparing two versions of a FHIR package can surface real upstream changes that are easy to miss
in a hand-maintained config file — renamed resource ids, or a package that quietly never made it
into the dependency manifest at all, despite being used. `IgDiff` reports, per category
(CodeSystems/Profiles/Extensions), what was added, removed, or — heuristically — renamed:

```java
IgPackageScanner scanner = new IgPackageScanner(new ObjectMapper());
IgPackageModel oldModel = scanner.scan(
    scanner.resolvePackageContentDir(fhirPackagesDir, packageName, "2025.0.0"), packageName, "2025.0.0");
IgPackageModel newModel = scanner.scan(
    scanner.resolvePackageContentDir(fhirPackagesDir, packageName, "2026.0.0"), packageName, "2026.0.0");

List<CategoryDiff> diffs = IgDiff.diff(oldModel, newModel);
```

Or from the command line: `IgDiffMain <fhirPackagesDir> <packageName> <oldVersion> <newVersion>`.

The rename heuristic flags an added/removed pair in the same category as a likely rename when one
constant's normalized id is fully contained in the other's (e.g. `VITALSTATUS` →
`MII_PR_PERSON_VITALSTATUS`, a real rename seen between MII kernel dataset versions). It's
best-effort: an IG author is free to rename an id beyond what containment can detect, in which case
it will surface as a plain add + remove instead.

## Known consumer-side gotcha: versioned Profile constants

`Profiles` constants bake in `|<version>` (FHIR's canonical-with-version convention used in
`meta.profile`). If you snapshot/golden-file test serialized FHIR JSON, you'll see spurious diffs
on every IG version bump. This is intentional — don't strip the version in the generator — instead
normalize/strip it in your test comparisons, e.g. a regex scrubber over `\|\d.*"` in your
golden-file approver.
