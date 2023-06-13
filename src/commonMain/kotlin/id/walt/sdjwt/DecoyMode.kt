package id.walt.sdjwt

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Mode for adding decoy digests on SD-JWT issuance
 * @property NONE: no decoy digests to be added (or mode is unknown, e.g. when parsing SD-JWTs)
 * @property FIXED: Fixed number of decoy digests to be added
 * @property RANDOM: Random number of decoy digests to be added
 */
@ExperimentalJsExport
@JsExport
enum class DecoyMode {
  NONE,
  FIXED,
  RANDOM
}