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
        // Matches: 
        // - v?X.Y.Z (simple format)
        // - v?X.Y.Z-P (Halfin patch format)
        // - v?X.Y.Z-N-gHASH (git describe from simple tag)
        // - v?X.Y.Z-P-N-gHASH (git describe from Halfin patch tag)
        // The regex uses a more flexible pattern to handle all cases
        // Pattern breakdown:
        // - v?(\\d+)\\.(\\d+)\\.(\\d+) - base version X.Y.Z
        // - (?:-(\\d+))? - optional patch number or commit count
        // - (?:-(\\d+)-g([a-zA-Z0-9]+))? - optional commit count and hash (for git describe)
        private val VERSION_REGEX = Regex("^v?(\\d+)\\.(\\d+)\\.(\\d+)(?:-(\\d+))?(?:-(\\d+)-g([a-zA-Z0-9]+))?$")

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
         * - X.Y.Z-N-gHASH (e.g., "0.3.2-5-gabc123" - git describe from simple tag)
         * - X.Y.Z-P-N-gHASH (e.g., "0.3.2-1-1-ga7a46bc" - git describe from Halfin patch tag)
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
                // group 4 is the first optional number (could be patch number or commit count)
                // group 5 is the second optional number (commit count, only if hash is present)
                // group 6 is the optional hash (git describe format)
                val firstNumber = m.groups[4]?.value?.toInt()
                val secondNumber = m.groups[5]?.value?.toInt()
                val hash = m.groups[6]?.value
                
                // Determine format based on what's present
                when {
                    hash != null -> {
                        // Git describe format: X.Y.Z-P-N-gHASH or X.Y.Z-N-gHASH
                        // If secondNumber is present, firstNumber is the patch number, secondNumber is commit count
                        // If secondNumber is null, firstNumber is the commit count
                        if (secondNumber != null) {
                            // Format: X.Y.Z-P-N-gHASH (from Halfin patch tag)
                            Version(major, minor, patch, secondNumber, hash)
                        } else {
                            // Format: X.Y.Z-N-gHASH (from simple tag)
                            Version(major, minor, patch, firstNumber, hash)
                        }
                    }
                    firstNumber != null -> {
                        // Halfin patch format: X.Y.Z-P (store patch in numCommits for comparison)
                        Version(major, minor, patch, firstNumber, null)
                    }
                    else -> {
                        // Simple format: X.Y.Z
                        Version(major, minor, patch, null, null)
                    }
                }
            }
        }
    }
}
