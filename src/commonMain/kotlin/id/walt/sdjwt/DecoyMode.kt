package id.walt.sdjwt

/**
 * Mode for adding decoy digests on SD-JWT issuance
 * @property NONE: no decoy digests to be added (or mode is unknown, e.g. when parsing SD-JWTs)
 * @property FIXED: Fixed number of decoy digests to be added
 * @property RANDOM: Random number of decoy digests to be added
 */
enum class DecoyMode {
  NONE,
  FIXED,
  RANDOM
}