package dev.gaphunter.rubynethttpreusecompanion.detect

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NetHttpLoopScannerTest {

    @Test
    fun `flags Net-HTTP-get inside an each-do loop`() {
        val code = """
            urls.each do |url|
              response = Net::HTTP.get(URI(url))
              puts response
            end
        """.trimIndent()
        val hits = NetHttpLoopScanner.scan(code)
        assertEquals(1, hits.size)
    }

    @Test
    fun `flags Net-HTTP-get_response inside a while loop`() {
        val code = """
            while has_more?
              response = Net::HTTP.get_response(uri)
              process(response)
            end
        """.trimIndent()
        val hits = NetHttpLoopScanner.scan(code)
        assertEquals(1, hits.size)
    }

    @Test
    fun `does not flag Net-HTTP-start reused connection inside a loop`() {
        val code = """
            Net::HTTP.start(host, port) do |http|
              urls.each do |url|
                response = http.get(url)
              end
            end
        """.trimIndent()
        val hits = NetHttpLoopScanner.scan(code)
        assertTrue(hits.isEmpty())
    }

    @Test
    fun `does not flag Net-HTTP-get outside of any loop`() {
        val code = """
            def fetch(url)
              Net::HTTP.get(URI(url))
            end
        """.trimIndent()
        assertTrue(NetHttpLoopScanner.scan(code).isEmpty())
    }

    @Test
    fun `loop ends correctly at end, does not leak into next method`() {
        val code = """
            def fetch_all(urls)
              urls.each do |url|
                Net::HTTP.get(URI(url))
              end
            end

            def fetch_one(url)
              Net::HTTP.get(URI(url))
            end
        """.trimIndent()
        val hits = NetHttpLoopScanner.scan(code)
        assertEquals(1, hits.size)
    }
}
