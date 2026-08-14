using IgCodegen;

string packageJsonFile = args.Length > 0 ? args[0] : Path.Combine("ig-codegen-cs", "package.json");
string fhirPackagesDir = args.Length > 1 ? args[1] : IgCodegenTool.DefaultFhirPackagesDir();
string outputDir =
    args.Length > 2 ? args[2] : Path.Combine("build", "generated", "sources", "ig-codegen-cs");

var generated = new IgCodegenTool().Generate(packageJsonFile, fhirPackagesDir, outputDir);
foreach (var file in generated)
    Console.WriteLine($"Generated {file}");
