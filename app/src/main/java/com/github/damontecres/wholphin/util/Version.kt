package com.github.damontecres.wholphin.util

import kotlinx.serialization.Serializable

@Serializable
data class Version(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val numCommits: Int? = null,
    val hash: String? = null,
) {
    /**
     * Is this version at least the given version
     */
    fun isAtLeast(version: Version): Boolean {
        if (this.major > version.major) {
            return true
        } else if (this.major == version.major) {
            if (this.minor > version.minor) {
                return true
            } else if (this.minor == version.minor) {
                if (this.patch > version.patch) {
                    return true
                } else if (this.patch == version.patch) {
                    if (this.compareNumCommits(version) >= 0) {
                        return true
                    }
                }
            }
        }
        return false
    }

    /**
     * Is this greater than the given version (and not equal to!)
     */
    fun isGreaterThan(version: Version): Boolean {
        if (this.major > version.major) {
            return true
        } else if (this.major == version.major) {
            if (this.minor > version.minor) {
                return true
            } else if (this.minor == version.minor) {
                if (this.patch > version.patch) {
                    return true
                } else if (this.patch == version.patch) {
                    if (this.compareNumCommits(version) > 0) {
                        return true
                    }
                }
            }
        }
        return false
    }

    /**
     * Is this less than the given version (and not equal to!)
     */
    fun isLessThan(version: Version): Boolean = this != version && isEqualOrBefore(version)

    /**
     * Is this equal to or before the specified version
     */
    fun isEqualOrBefore(version: Version): Boolean = !isGreaterThan(version)

    private fun compareNumCommits(version: Version): Int = (this.numCommits ?: 0) - (version.numCommits ?: 0)

    override fun toString(): String =
        when {
            numCommits != null && hash != null -> {
                // Git describe format
                "v$major.$minor.$patch-$numCommits-g$hash"
            }
            numCommits != null && hash == null -> {
                // Halfin patch format
                "$major.$minor.$patch-$numCommits"
            }
            else -> {
                // Simple format
                "v$major.$minor.$patch"
            }
        }

    companion object {
        // Matches: v?X.Y.Z, v?X.Y.Z-P (Halfin patch format), or v?X.Y.Z-N-gHASH (git describe format)
        private val VERSION_REGEX = Regex("v?(\\d+)\\.(\\d+)\\.(\\d+)(?:-(\\d+)(?:-g([a-zA-Z0-9]+))?)?")

        /**
         * Parse a version string throwing if it is invalid
         */
        fun fromString(version: String): Version {
            val v = tryFromString(version)
            if (v == null) {
                throw IllegalArgumentException(version)
            } else {
                return v
            }
        }

        /**
         * Attempt to parse a version string or else return null
         * Supports formats:
         * - X.Y.Z (e.g., "0.3.2")
         * - X.Y.Z-P (e.g., "0.3.2-1" - Halfin patch format)
         * - X.Y.Z-N-gHASH (e.g., "0.3.2-5-gabc123" - git describe format)
         */
        fun tryFromString(version: String?): Version? {
            if (version == null) {
                return null
            }
            val m = VERSION_REGEX.matchEntire(version)
            return if (m == null) {
                null
            } else {
                val major = m.groups[1]!!.value.toInt()
                val minor = m.groups[2]!!.value.toInt()
                val patch = m.groups[3]!!.value.toInt()
                // group 4 is the optional number (patch number or commit count)
                // group 5 is the optional hash (if present, it's git describe format)
                val numOrPatch = m.groups[4]?.value?.toInt()
                val hash = m.groups[5]?.value
                
                // If hash is present, it's git describe format (numCommits-hash)
                // Otherwise, if numOrPatch is present, it's Halfin patch format
                if (hash != null) {
                    // Git describe format: X.Y.Z-N-gHASH
                    Version(major, minor, patch, numOrPatch, hash)
                } else if (numOrPatch != null) {
                    // Halfin patch format: X.Y.Z-P (store patch in numCommits for comparison)
                    Version(major, minor, patch, numOrPatch, null)
                } else {
                    // Simple format: X.Y.Z
                    Version(major, minor, patch, null, null)
                }
            }
        }
    }
}
