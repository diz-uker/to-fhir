# to-fhir

[![OpenSSF Scorecard](https://img.shields.io/ossf-scorecard/github.com/diz-uker/to-fhir?label=openssf%20scorecard&style=flat)](https://scorecard.dev/viewer/?uri=github.com/diz-uker/to-fhir)

Collection of utilities for mapping FHIR resources.

This repository contains two artifacts:

- `to-fhir` — the core library. Plain Java, no Spring Boot dependency.
- `to-fhir-starter` — a Spring Boot starter that auto-configures `FhirProperties`
  (overridable via `fhir.*` properties) on top of `to-fhir`.

## Installation

### Gradle

<!-- x-release-please-start-version -->

```groovy
implementation "io.github.diz-uker:to-fhir:0.2.11"

// optional, for Spring Boot auto-configuration
implementation "io.github.diz-uker:to-fhir-starter:0.2.11"
```

<!-- x-release-please-end -->

### Maven

<!-- x-release-please-start-version -->

```xml
<dependency>
    <groupId>io.github.diz-uker</groupId>
    <artifactId>to-fhir</artifactId>
    <version>0.2.11</version>
</dependency>

<!-- optional, for Spring Boot auto-configuration -->
<dependency>
    <groupId>io.github.diz-uker</groupId>
    <artifactId>to-fhir-starter</artifactId>
    <version>0.2.11</version>
</dependency>
```

<!-- x-release-please-end -->

## Development

### Generating C# FHIR IG constants

The generated C# constant classes live in `fhir-ig-constants-cs/src/`.
They are produced from `fhir-ig-constants/package.json` by the `ig-codegen-cs` tool
and must be re-generated whenever the package manifest or the codegen itself changes.

**1. Install FHIR packages**

```sh
cd fhir-ig-constants && npm install
```

**2. Re-generate and format**

```sh
dotnet msbuild fhir-ig-constants-cs -t:GenerateIgConstants
dotnet tool restore && dotnet csharpier format fhir-ig-constants-cs/src/
```

Review the diff, then commit. The `fhir-ig-constants.yaml` CI workflow fails if the
checked-in files are out of date or not formatted.

Alternatively, invoke the tool directly (useful if you want to point at a different
packages directory):

```sh
dotnet run --project ig-codegen-cs/IgCodegen/IgCodegen.csproj -- \
  fhir-ig-constants/package.json \
  fhir-ig-constants/node_modules \
  fhir-ig-constants-cs/src/
```

### Snapshot testing

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
