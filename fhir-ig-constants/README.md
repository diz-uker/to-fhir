# fhir-ig-constants

Generated Java constant classes for the canonical `CodeSystem`/`StructureDefinition`
(Profile)/`Extension` URLs of whatever FHIR Implementation Guides this project depends on, via
[`ig-codegen`](../ig-codegen). A single artifact for all of them, kept simple: one Java class per
IG package, all published together under one Maven coordinate.

`ig-codegen` itself is only needed at generation time (wired up as a `codegen` configuration, not
`api`/`implementation`). The generated code does carry one real runtime dependency, though:
`hapi-fhir-structures-r4` (`api` scope) — CodeSystems with inline concepts get a generated enum
whose `coding()` accessor returns a `org.hl7.fhir.r4.model.Coding`.

## Setup

1. Add a `package.json` here listing the FHIR packages to generate from, e.g.:

   ```json
   {
     "name": "fhir-ig-constants",
     "version": "0.1.0",
     "dependencies": {
       "hl7.fhir.r4.core": "4.0.1",
       "de.medizininformatikinitiative.kerndatensatz.base": "2026.0.0",
       "de.medizininformatikinitiative.kerndatensatz.onkologie": "2026.0.3"
     },
     "fhirVersions": ["4.0.1"]
   }
   ```

2. Restore those packages locally, either with `fhir restore` (Firely Terminal CLI) or `npm
   install`, reading the same `package.json` — both work, `generateIgConstants` tries
   `~/.fhir/packages`, then `./.fhir/packages`, then `./node_modules`, in that order.
3. Run `./gradlew :fhir-ig-constants:generateIgConstants`.
4. Review the diff in `src/main/java`, commit it.

## Usage

```java
import de.medizininformatikinitiative.kerndatensatz.onkologie.Onkologie;

String profileUrl = Onkologie.Profiles.miiPrOnkoOperation();

// CodeSystems with inline concepts (content == "complete") also get an enum with a coding() accessor:
Coding intention = Onkologie.CodeSystems.MiiCsOnkoIntention.K.coding();
```

## Updating an IG version

Bump the version in `package.json`, `fhir restore` again, rerun `generateIgConstants`, review the
diff (added/removed/renamed constants — see `IgDiff` in `ig-codegen` if you want an explicit report
before regenerating), commit.
