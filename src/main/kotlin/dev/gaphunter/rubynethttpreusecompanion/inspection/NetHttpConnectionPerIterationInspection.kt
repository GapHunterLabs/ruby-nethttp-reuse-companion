package dev.gaphunter.rubynethttpreusecompanion.inspection

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import dev.gaphunter.rubynethttpreusecompanion.detect.NetHttpLoopScanner
import dev.gaphunter.rubynethttpreusecompanion.review.ReviewPrompt

/**
 * Flags a `Net::HTTP.get`/`.get_response`/`.post` shorthand call found
 * inside a loop/iterator block -- see [NetHttpLoopScanner] for the
 * full reasoning. Runs via [checkFile] (whole-file text scan), same
 * discipline as `ruby-gemfile-group-companion`'s
 * `UngroupedDevTestGemInspection` -- see `build.gradle.kts` for why no
 * Ruby-language PSI dependency is taken.
 */
class NetHttpConnectionPerIterationInspection : LocalInspectionTool() {

    companion object {
        const val MAX_FILE_LENGTH = 500_000
        private val RUBY_FILE_NAME = Regex("""^[^.]+\.rb$""", RegexOption.IGNORE_CASE)
    }

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        val virtualFile = file.virtualFile ?: return null
        if (!RUBY_FILE_NAME.matches(virtualFile.name)) return null

        val text = file.text
        if (text.length > MAX_FILE_LENGTH) return null

        val hits = NetHttpLoopScanner.scan(text)
        if (hits.isEmpty()) return null

        val document = file.viewProvider.document ?: return null
        val problems = mutableListOf<ProblemDescriptor>()

        for (hit in hits) {
            if (hit.lineNumber - 1 !in 0 until document.lineCount) continue
            val lineStartOffset = document.getLineStartOffset(hit.lineNumber - 1)
            val lineEndOffset = document.getLineEndOffset(hit.lineNumber - 1)
            val anchor = leafElementAt(file, lineStartOffset) ?: continue
            val anchorStart = anchor.textRange.startOffset
            val relativeRange = TextRange(
                (lineStartOffset - anchorStart).coerceAtLeast(0),
                (lineEndOffset - anchorStart).coerceAtMost(anchor.textLength),
            )
            if (relativeRange.startOffset >= relativeRange.endOffset) continue

            problems += manager.createProblemDescriptor(
                anchor,
                relativeRange,
                "${hit.callText}(...) opens and closes a new connection on every call -- inside a loop, this pays " +
                    "a TCP round-trip per iteration. Use Net::HTTP.start(...) once outside the loop and reuse it",
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                isOnTheFly,
            )

            ReviewPrompt.recordHit(file.project, "${virtualFile.path}:${hit.lineNumber}")
        }

        return if (problems.isEmpty()) null else problems.toTypedArray()
    }

    private fun leafElementAt(file: PsiFile, startOffset: Int): PsiElement? {
        if (startOffset < 0 || startOffset >= file.textLength) return null
        var element = file.findElementAt(startOffset) ?: return file
        while (element.firstChild != null) {
            element = element.firstChild
        }
        return element
    }
}
