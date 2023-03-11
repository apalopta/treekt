package de.apalopta.treekt

import java.io.File
import kotlin.math.min

/** Creates the directory tree output. */
class FileLister(private val args: Arguments) {
    private val format = args.format.type
    private val dir = args.dir

    fun display() {
        println(listing())
    }

    /** Returns the directory listing as String. */
    fun listing(): String = buildString {
        append(dir.path)
        append(listDir(dir, 1))
    }

    /** List directories, then files. */
    private fun listDir(dir: File, level: Int, prefix: String = ""): String = buildString {
        if (level <= args.levels) {
            dir.listFiles()?.run {
                val (dirs, files) = partition { it.isDirectory }
                dirs.listAsDirs(this@buildString, prefix, level)
                if (args.showFiles) {
                    files.listAsFiles(this@buildString, prefix)
                }
            }
        }
    }

    private fun String.toCurrentDirPrefix(isRunningDir: Boolean) =
        if (isRunningDir) "$this${format.runningDirSymbol}" else "$this${format.lastDirSymbol}"

    private fun List<File>.listAsDirs(sb: StringBuilder, prefix: String, level: Int) {
        val nrOfDisplayedDirs = args.limitDirsTo
        var lastDisplayedIndex: Int

        filter { it.shallDisplay() }
            .filterIndexed { i, _ -> i < (nrOfDisplayedDirs + 1) }
            .also { lastDisplayedIndex = min(it.size, nrOfDisplayedDirs) - 1 }
            .forEachIndexed { i, dir ->
                if (i <= lastDisplayedIndex) {
                    val relevantIndex = if (lastDisplayedIndex < nrOfDisplayedDirs - 1) lastDisplayedIndex else nrOfDisplayedDirs
                    val thisLinesPrefix = prefix.toCurrentDirPrefix(i < relevantIndex)
                    sb.appendLine().append("$thisLinesPrefix${dir.name}")
                    sb.append(listDir(dir, level + 1, format.prefixForNextLevel(thisLinesPrefix)))
                } else {
                    sb.appendLine().append("$prefix${format.lastDirSymbol}${format.anonymous}")
                }
            }
    }

    private fun List<File>.listAsFiles(sb: StringBuilder, prefix: String) {
        val limitTo = args.limitFilesTo
        filter { it.shallDisplay() }
            .filterIndexed { i, _ -> i < (limitTo + 1) }
            .forEachIndexed { i, file ->
                val entry = if (i < limitTo) file.name else format.anonymous
                sb.appendLine().append("$prefix${entry}")
            }
    }

    private fun File.shallDisplay() = dontHide() && dontSkip() && dontSkipInGeneral()

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

    private fun File.dontSkipInGeneral() = args.skip.none { it.containsMatchIn(toURI().toString()) }
}
