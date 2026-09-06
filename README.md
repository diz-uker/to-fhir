# to-fhir

[![OpenSSF Scorecard](https://img.shields.io/ossf-scorecard/github.com/diz-uker/to-fhir?label=openssf%20scorecard&style=flat)](https://scorecard.dev/viewer/?uri=github.com/diz-uker/to-fhir)

Collection of utilities for mapping FHIR resources.

This repository contains the following artifacts:

- `to-fhir` — the core library. Plain Java, no Spring Boot dependency.
- `to-fhir-starter` — a Spring Boot starter that auto-configures `FhirProperties`
  (overridable via `fhir.*` properties) on top of `to-fhir`.
- `DizUker.ToFhir` — the C# port of the core library, built on the
  [Firely SDK](https://github.com/FirelyTeam/firely-net-sdk) instead of HAPI FHIR.
  Same behaviour, idiomatic .NET API; see [`to-fhir-cs/`](to-fhir-cs/).

## Installation

### Gradle

<!-- x-release-please-start-version -->

```groovy
implementation "io.github.diz-uker:to-fhir:0.2.18"

// optional, for Spring Boot auto-configuration
implementation "io.github.diz-uker:to-fhir-starter:0.2.18"
```

<!-- x-release-please-end -->

### Maven

<!-- x-release-please-start-version -->

```xml
<dependency>
    <groupId>io.github.diz-uker</groupId>
    <artifactId>to-fhir</artifactId>
    <version>0.2.18</version>
</dependency>

<!-- optional, for Spring Boot auto-configuration -->
<dependency>
    <groupId>io.github.diz-uker</groupId>
    <artifactId>to-fhir-starter</artifactId>
    <version>0.2.18</version>
</dependency>
```

<!-- x-release-please-end -->

### .NET

<!-- x-release-please-start-version -->

```sh
dotnet add package DizUker.ToFhir --version 0.2.18
```

<!-- x-release-please-end -->

## Usage (C#)

```csharp
using Hl7.Fhir.Model;
using ToFhir;

var patient = new Patient
{
    Id = IdUtils.FromIdentifier(new Identifier("https://example.org/pid", "12345")),
};

var bundle = new TransactionBuilder()
    .WithId("my-bundle")
    .WithFullUrlBase("https://example.org/fhir")
    .WithProvenance(
        new Device { Id = "my-etl-job" },
        new ResourceReference { Display = "The source system" })
    .AddEntry(patient)
    .Build();
```

`BuildWithSeparateProvenance()` returns the data and Provenance resources as two
bundles instead of one:

```csharp
var (dataBundle, provenanceBundle) = new TransactionBuilder()
    .WithProvenance(who, what)
    .AddEntries(patient, observation)
    .BuildWithSeparateProvenance();
```

The remaining helpers mirror the Java library: `FhirSystems` (canonical system
URIs), `FhirCodings` (system + version `Coding` templates), `FhirExtensions`
together with the `DataAbsentReasonCode` enum, and `ReferenceUtils`.

## Development

### Generating FHIR IG constants

The generated constant classes in `fhir-ig-constants/src/main/java/` (Java),
`fhir-ig-constants-cs/src/` (C#), and `fhir-ig-constants-py/src/` (Python) are all
produced from `fhir-ig-constants/package.json` and must be re-generated whenever the
package manifest or the codegen tools change.

**1. Install FHIR packages**

```sh
cd fhir-ig-constants && npm install
```

**2a. Re-generate Java**

```sh
./gradlew :fhir-ig-constants:generateIgConstants
```

Spotless formatting is applied automatically. Review the diff, then commit.

**2b. Re-generate C#**

```sh
dotnet msbuild fhir-ig-constants-cs -t:GenerateIgConstants
dotnet tool restore && dotnet csharpier format fhir-ig-constants-cs/src/
```

**2c. Re-generate Python**

```sh
cd ig-codegen-py
uv run ig-codegen \
  ../fhir-ig-constants/package.json \
  ../fhir-ig-constants/node_modules \
  ../fhir-ig-constants-py/src/
uv run ruff format \
  --config ../fhir-ig-constants-py/pyproject.toml \
  ../fhir-ig-constants-py/src/
```

Review the diff, then commit.

### C# tests

```sh
dotnet test to-fhir-cs/ToFhir.Tests/
dotnet csharpier format .
```

The C# snapshot tests use [Verify](https://github.com/VerifyTests/Verify); its
snapshots live in `to-fhir-cs/ToFhir.Tests/Snapshots/`. A changed snapshot is
approved by renaming the generated `*.received.json` to `*.verified.json`.

### Snapshot testing (Java)

#### Auto-approve snapshot changes

Usually, approving a changed snapshots requires manually renaming or moving the
snapshot file from `.received.` to `.approved.`.
If you are facing a lot of changed snapshots and are certain that your changes
are valid, you can automatically approve them:

```sh
APPROVAL_TESTS_USE_REPORTER=AutoApproveReporter ./gradlew test
```

Source: <https://github.com/approvals/ApprovalTests.Java/issues/590>.

You can also run this in a loop to approve indexed snapshots:

```sh
for i in {1..10};
    do APPROVAL_TESTS_USE_REPORTER=AutoApproveReporter ./gradlew test;
done
```
