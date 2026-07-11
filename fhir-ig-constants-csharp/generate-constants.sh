#!/usr/bin/env bash
# Regenerates the constant classes in src/FhirIgConstants from package.json. Review the diff and
# commit. Manually-invoked "generate, review, commit" dev tool, not a build-graph participant -
# mirrors fhir-ig-constants/build.gradle's generateIgConstants task.
set -euo pipefail
cd "$(dirname "$0")"

# Prefer the global Firely Terminal cache; fall back to a project-local ./.fhir/packages if it's
# missing (e.g. when packages were restored relative to this directory instead), then to
# ./node_modules (FHIR packages installed via 'npm install' rather than 'fhir restore').
home_fhir_packages_dir="${HOME}/.fhir/packages"
cwd_fhir_packages_dir="$(pwd)/.fhir/packages"
if [ -d "$home_fhir_packages_dir" ]; then
  fhir_packages_dir="$home_fhir_packages_dir"
elif [ -d "$cwd_fhir_packages_dir" ]; then
  fhir_packages_dir="$cwd_fhir_packages_dir"
else
  fhir_packages_dir="$(pwd)/node_modules"
fi

dotnet run --project ../ig-codegen-csharp/src/IgCodegen -- package.json "$fhir_packages_dir" src/FhirIgConstants
