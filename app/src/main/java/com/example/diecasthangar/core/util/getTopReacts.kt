package com.example.diecasthangar.core.util

import com.example.diecasthangar.data.getReacts

/**
 * Given an input map, return a list of pairs containing the top 3 pairs by value
 * starts with dummy elements for posts with fewer than 3 react types
 *
 * @param reacts the map to be sorted
 */
fun getTopReacts(reactions: Map<String, Int>, limit: Int): List<Pair<String, Int>> {
    val baseReacts = hashMapOf(
        "dummy1" to 0,
        "dummy2" to 0,
        "dummy3" to 0
    )
    val reacts = baseReacts.plus(reactions)
    return reacts.toList().sortedBy { (_, value) -> value }.reversed().take(limit)
}