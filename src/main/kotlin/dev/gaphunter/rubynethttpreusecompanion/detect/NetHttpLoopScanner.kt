package dev.gaphunter.rubynethttpreusecompanion.detect

import dev.gaphunter.rubynethttpreusecompanion.model.ConnectionPerIterationHit

/**
 * Plain-text line scanner for a Ruby file -- flags a call to
 * `Net::HTTP.get`/`.get_response`/`.post` (the shorthand,
 * non-persistent-connection form) found inside a loop/iterator block
 * (`.each do`, `.times do`, `while ... do`, `for ... do`, brace-block
 * form `{ }`). Ruby's own `Net::HTTP` documentation explains why this
 * matters: "Creating a new HTTP connection for every request involves
 * an extra TCP round-trip and causes TCP congestion avoidance
 * negotiation to start over" -- the shorthand class methods open and
 * close a connection on every single call, so using them inside a
 * loop means paying that cost on every iteration instead of once via
 * `Net::HTTP.start(...)`.
 *
 * **Deliberately line-based, not a real Ruby parser** -- same
 * discipline as `ruby-gemfile-group-companion`'s `GemfileScanner`.
 * Tracks loop-block `do`/`end` (and brace-block `{`/`}`) nesting depth
 * via a simple counter, entered only by a recognized iterator/loop
 * header line.
 *
 * **v0.1 scope, stated honestly:** only recognizes the common
 * `.each do`/`.times do`/`while ... do`/`for ... in ... do`/brace-block
 * loop-header shapes -- a loop built some other way (recursion,
 * `loop { }` without those exact keywords nearby) isn't specially
 * tracked. Matches by simple text, not real call resolution -- an
 * unrelated `.get`/`.post` method on some other object is a possible
 * (rare) false positive.
 */
object NetHttpLoopScanner {

    private val LOOP_HEADER = Regex("""\.(each|each_with_index|times|map|select|loop)\b.*\bdo\b\s*(\|[^|]*\|)?\s*$""")
    // `do` is optional on a multi-line `while`/`until` header in real Ruby syntax (`while x` alone is valid) --
    // `for ... in ... do` conventionally keeps the `do`, but is accepted either way here too.
    private val WHILE_FOR_HEADER = Regex("""^\s*(while|until|for)\b.*$""")
    private val BRACE_LOOP_HEADER = Regex("""\.(each|each_with_index|times|map|select)\b.*\{\s*(\|[^|]*\|)?\s*$""")
    private val BLOCK_END = Regex("""^\s*end\s*$""")
    private val OTHER_DO_BLOCK = Regex(""".*\bdo\b\s*(\|[^|]*\|)?\s*$""")
    private val NET_HTTP_CALL = Regex("""Net::HTTP\.(get_response|get|post)\(""")

    fun scan(text: String): List<ConnectionPerIterationHit> {
        val hits = mutableListOf<ConnectionPerIterationHit>()
        var loopDoDepth = 0
        var otherDoDepth = 0
        var braceDepth = 0

        text.lines().forEachIndexed { index, rawLine ->
            val trimmed = rawLine.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#")) return@forEachIndexed

            when {
                loopDoDepth == 0 && braceDepth == 0 && (LOOP_HEADER.containsMatchIn(trimmed) || WHILE_FOR_HEADER.matches(trimmed)) -> {
                    loopDoDepth++
                    return@forEachIndexed
                }
                loopDoDepth == 0 && braceDepth == 0 && BRACE_LOOP_HEADER.containsMatchIn(trimmed) -> {
                    braceDepth++
                    return@forEachIndexed
                }
                loopDoDepth > 0 && braceDepth == 0 && OTHER_DO_BLOCK.containsMatchIn(trimmed) -> {
                    otherDoDepth++
                }
                loopDoDepth > 0 && braceDepth == 0 && otherDoDepth > 0 && BLOCK_END.matches(trimmed) -> {
                    otherDoDepth--
                }
                loopDoDepth > 0 && braceDepth == 0 && otherDoDepth == 0 && BLOCK_END.matches(trimmed) -> {
                    loopDoDepth--
                    return@forEachIndexed
                }
                braceDepth > 0 -> {
                    braceDepth += trimmed.count { it == '{' } - trimmed.count { it == '}' }
                    if (braceDepth <= 0) {
                        braceDepth = 0
                        return@forEachIndexed
                    }
                }
            }

            if (loopDoDepth == 0 && braceDepth == 0) return@forEachIndexed

            val match = NET_HTTP_CALL.find(trimmed) ?: return@forEachIndexed
            hits += ConnectionPerIterationHit(match.value.trimEnd('('), index + 1)
        }

        return hits
    }
}
