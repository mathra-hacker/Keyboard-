package ime.suggest

/**
 * A minimal Trie implementation for prefix search.
 * Designed to be small and dependency-free. Assumes most words fit in BMP (Sinhala and English)
 * and iterates chars. Stores a weight for simple ranking.
 */
class Trie {
    private class Node(var weight: Int = 0) {
        val children: MutableMap<Char, Node> = mutableMapOf()
        var isWord: Boolean = false
    }

    private val root = Node()

    // Insert a word with optional weight (frequency). Overwrites if same word inserted again.
    fun insert(word: String, weight: Int = 1) {
        if (word.isEmpty()) return
        var node = root
        for (c in word) {
            node = node.children.getOrPut(c) { Node() }
        }
        node.isWord = true
        node.weight = node.weight + weight
    }

    // Simple existence check
    fun contains(word: String): Boolean {
        if (word.isEmpty()) return false
        var node = root
        for (c in word) {
            node = node.children[c] ?: return false
        }
        return node.isWord
    }

    // Collect top N results that start with prefix. Returns list of pairs (word, weight)
    fun prefixSearch(prefix: String, maxResults: Int = 10): List<Pair<String, Int>> {
        if (prefix.isEmpty()) return emptyList()
        var node = root
        for (c in prefix) {
            node = node.children[c] ?: return emptyList()
        }
        val results = mutableListOf<Pair<String, Int>>()
        val sb = StringBuilder(prefix)

        fun dfs(cur: Node, builder: StringBuilder) {
            if (results.size >= maxResults * 8) {
                // allow deeper exploration but we will prune later
            }
            if (cur.isWord) {
                results.add(builder.toString() to cur.weight)
            }
            for ((ch, child) in cur.children) {
                builder.append(ch)
                dfs(child, builder)
                builder.setLength(builder.length - 1)
            }
        }

        dfs(node, sb)
        // sort by weight desc then lexicographically
        return results.sortedWith(compareByDescending<Pair<String, Int>> { it.second }
            .thenBy { it.first }).take(maxResults)
    }
}
