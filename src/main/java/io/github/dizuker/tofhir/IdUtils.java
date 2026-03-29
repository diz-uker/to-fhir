package io.github.dizuker.tofhir;

import java.security.MessageDigest;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.Validate;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;

/** Utility class for FHIR resource Ids */
public class IdUtils {
  private IdUtils() {
    // Utility class, prevent instantiation
  }

  /**
   * Computes a deterministic ID from a FHIR Identifier by hashing the system and value using
   * SHA-256.
   *
   * @param identifier the FHIR Identifier to compute the ID from
   * @return a deterministic ID string derived from the identifier's system and value
   */
  public static IIdType fromIdentifier(Identifier identifier) {
    return fromIdentifier(identifier, DigestUtils.getSha256Digest());
  }

  /**
   * Computes a deterministic ID from a FHIR Identifier using the provided MessageDigest algorithm.
   *
   * @param identifier the FHIR Identifier to compute the ID from
   * @param digest the MessageDigest to use for hashing (e.g., SHA-256)
   * @return a deterministic ID string derived from the identifier's system and value
   */
  public static IIdType fromIdentifier(Identifier identifier, MessageDigest digest) {
    Validate.notBlank(identifier.getSystem());
    Validate.notBlank(
        identifier.getValue(),
        "Identifier value must not be blank. System: %s",
        identifier.getSystem());

    var stringId =
        new DigestUtils(digest).digestAsHex(identifier.getSystem() + "|" + identifier.getValue());
    return new IdType(stringId);
  }
}
