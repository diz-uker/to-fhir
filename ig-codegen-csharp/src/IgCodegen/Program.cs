using Dizuker.IgCodegen;

string packageJsonFile = args.Length > 0 ? args[0] : Path.Combine("ig-codegen-csharp", "package.json");
string fhirPackagesDir = args.Length > 1 ? args[1] : IgCodegenTool.DefaultFhirPackagesDir();
string outputDir = args.Length > 2 ? args[2] : Path.Combine("bin", "generated-sources", "ig-codegen");

List<string> generatedFiles = new IgCodegenTool().Generate(packageJsonFile, fhirPackagesDir, outputDir);
foreach (string file in generatedFiles)
{
    Console.WriteLine($"Generated {file}");
}
