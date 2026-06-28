package io.github.dizuker.igcodegen;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/**
 * A FHIR package manifest ({@code package.json}, restored by Firely Terminal's {@code fhir
 * restore}). Despite the filename, this is the FHIR package ecosystem's manifest shape, not npm's.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PackageManifest(String name, String version, Map<String, String> dependencies) {

  public static PackageManifest read(Path packageJsonFile, ObjectMapper objectMapper)
      throws IOException {
    return objectMapper.readValue(packageJsonFile.toFile(), PackageManifest.class);
  }
}
