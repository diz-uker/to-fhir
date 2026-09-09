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
- `to-fhir` (PyPI) — the Python port of the core library, built on
  [fhir.resources](https://github.com/nazrulworld/fhir.resources). Same behaviour,
  idiomatic Python API; see [`to-fhir-py/`](to-fhir-py/).

## Installation

### Gradle

<!-- x-release-please-start-version -->

```groovy
implementation "io.github.diz-uker:to-fhir:0.2.19"

// optional, for Spring Boot auto-configuration
implementation "io.github.diz-uker:to-fhir-starter:0.2.19"
```

<!-- x-release-please-end -->

### Maven

<!-- x-release-please-start-version -->

```xml
<dependency>
    <groupId>io.github.diz-uker</groupId>
    <artifactId>to-fhir</artifactId>
    <version>0.2.19</version>
</dependency>

<!-- optional, for Spring Boot auto-configuration -->
<dependency>
    <groupId>io.github.diz-uker</groupId>
    <artifactId>to-fhir-starter</artifactId>
    <version>0.2.19</version>
</dependency>
```

<!-- x-release-please-end -->

### .NET

<!-- x-release-please-start-version -->

```sh
dotnet add package DizUker.ToFhir --version 0.2.19
```

<!-- x-release-please-end -->

### Python

<!-- x-release-please-start-version -->

```sh
uv add to-fhir==0.2.19
# or
pip install to-fhir==0.2.19
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

## Usage (Python)

```python
from fhir.resources.R4B.device import Device
from fhir.resources.R4B.identifier import Identifier
from fhir.resources.R4B.patient import Patient
from fhir.resources.R4B.reference import Reference

from to_fhir import TransactionBuilder, id_utils

patient = Patient(
    id=id_utils.from_identifier(Identifier(system="https://example.org/pid", value="12345"))
)

bundle = (
    TransactionBuilder()
    .with_id("my-bundle")
    .with_full_url_base("https://example.org/fhir")
    .with_provenance(Device(id="my-etl-job"), Reference(display="The source system"))
    .add_entry(patient)
    .build()
)
```

`build_with_separate_provenance()` returns the data and Provenance resources as
two bundles instead of one, as a named tuple you can unpack:

```python
data_bundle, provenance_bundle = (
    TransactionBuilder()
    .with_provenance(who, what)
    .add_entries(patient, observation)
    .build_with_separate_provenance()
)
```

The remaining helpers mirror the Java library: `fhir_systems` (canonical system
URIs), `fhir_codings` (system + version `Coding` templates), `fhir_extensions`
together with the `DataAbsentReason` enum, and `reference_utils`.

Both `to-fhir` and the generated `fhir-ig-constants` package target R4B
(`fhir.resources.R4B`), the closest release `fhir.resources` offers to the R4
structures the Java and C# libraries build on, so their models interoperate — a
`Coding` from either can go into the same `CodeableConcept`.

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

### Python tests

```sh
cd to-fhir-py
uv run pytest
uv run ruff check src/ tests/ && uv run ruff format src/ tests/
```

The Python snapshot tests use [syrupy](https://github.com/syrupy-project/syrupy);
its snapshots live in `to-fhir-py/tests/__snapshots__/`. Changed snapshots are
approved with `uv run pytest --snapshot-update`.

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
