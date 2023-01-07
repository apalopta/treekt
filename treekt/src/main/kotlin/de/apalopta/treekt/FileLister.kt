package de.apalopta.treekt

import java.io.File
import kotlin.math.min

/** Creates the directory tree output. */
class FileLister(private val args: Arguments) {
    private var sb: StringBuilder = java.lang.StringBuilder()

    companion object {
        const val MAX_DEPTH = 256
    }

    fun display() {
        println("----")
        println(list())
        println("----")
    }

    fun list(): String =
        if (sb.isNotEmpty())
            sb.toString()
        else
            sb.append(args.dir.path).append(listDir(args.dir, 1)).toString()


    private fun listDir(dir: File, level: Int, prefix: String = ""): String {
        val sb = StringBuilder()
        val (dirs, files) = dir.listFiles().partition { it.isDirectory }

        if (level <= args.levels) {
            showDirs(dirs, sb, prefix, level)
            if (args.showFiles) {
                showFiles(files, sb, prefix)
            }
        }

        return sb.toString()
    }

    private fun String.prefixForNextLevel() = replace(Regex("""[\\-]"""), " ").replace('+', '|')
    private fun String.prefixForCurrentDir(isLastDisplayed: Boolean) = if (isLastDisplayed) "$this\\---" else "$this+---"

    private fun showDirs(dirs: List<File>, sb: StringBuilder, prefix: String, level: Int) {
        var nrOfDirs: Int
        var appendAnonymous: Boolean
        var lastDisplayedIndex: Int
        dirs.filter { it.wouldDisplay() }.also {
            nrOfDirs = min(it.size, args.limitDirsTo)
            appendAnonymous = it.size > args.limitDirsTo
            lastDisplayedIndex = if (appendAnonymous) nrOfDirs else nrOfDirs - 1
        }.take(nrOfDirs).onEachIndexed { index, currentDir ->
            val thisLinesPrefix = prefix.prefixForCurrentDir(index == lastDisplayedIndex)
            sb.appendLine().append("$thisLinesPrefix${currentDir.name}")
            sb.append(listDir(currentDir, level + 1, thisLinesPrefix.prefixForNextLevel()))
        }.also {
            if (appendAnonymous) {
                sb.appendLine().append("$prefix\\---...")
            }
        }
    }

    private fun showFiles(files: List<File>, sb: StringBuilder, prefix: String) {
        var nrOfFiles: Int
        var appendAnonymous: Boolean
        files.filter { it.wouldDisplay() }.also {
            nrOfFiles = min(it.size, args.limitFilesTo)
            appendAnonymous = it.size > args.limitFilesTo
        }.take(nrOfFiles).forEach { sb.appendLine().append("$prefix${it.name}") }.also {
            if (appendAnonymous) {
                sb.appendLine().append("$prefix...")
            }
        }
    }

    private fun File.wouldDisplay() = dontHide() && dontSkip() && dontMatchGlobalSkip()

    private fun File.dontHide() = args.hideType == HideType.NONE ||
            !(isHidden && ((args.hideType == HideType.ALL_SYSTEM_FILES)
                    || (isFile && (args.hideType == HideType.SYSTEM_FILES_FILES_ONLY))
                    || (isDirectory && args.hideType != HideType.SYSTEM_FILES_DIRECTORIES_ONLY))
                    )

    private fun File.dontSkip() = if (isFile) {
        args.skipFiles.isEmpty() || args.skipFiles.none { toURI().toString().contains(it) }
    } else {
        args.skipDirectories.isEmpty() || args.skipDirectories.none { toURI().toString().contains(it) }
    }

    private fun File.dontMatchGlobalSkip() = args.skip.none { it.containsMatchIn(toURI().toString()) }
}
