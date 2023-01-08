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
    fun listing(): String = StringBuilder()
        .append(dir.path)
        .append(listDir(dir, 1))
        .toString()

    /** List directories, then files. */
    private fun listDir(dir: File, level: Int, prefix: String = ""): String {
        val sb = StringBuilder()

        if (level <= args.levels) {
            val (dirs, files) = dir.listFiles().partition { it.isDirectory }
            dirs.listAsDirs(sb, prefix, level)
            if (args.showFiles) {
                files.listAsFiles(sb, prefix)
            }
        }

        return sb.toString()
    }

    private fun String.toCurrentDirPrefix(isLastDisplayed: Boolean) =
        if (isLastDisplayed) "$this${format.lastDirSymbol}" else "$this${format.runningDirSymbol}"

    private fun List<File>.listAsDirs(sb: StringBuilder, prefix: String, level: Int) {
        var nrOfItems: Int
        var appendAnonymous: Boolean
        var lastDisplayedIndex: Int
        filter { it.shallDisplay() }
            .also {
                nrOfItems = min(it.size, args.limitDirsTo)
                appendAnonymous = nrOfItems < it.size
                lastDisplayedIndex = if (appendAnonymous) nrOfItems else nrOfItems - 1
            }
            .take(nrOfItems)
            .forEachIndexed { index, currentDir ->
                val thisLinesPrefix = prefix.toCurrentDirPrefix(index == lastDisplayedIndex)
                sb.appendLine().append("$thisLinesPrefix${currentDir.name}")
                sb.append(listDir(currentDir, level + 1, format.prefixForNextLevel(thisLinesPrefix)))
            }
        if (appendAnonymous) {
            sb.appendLine().append("$prefix${format.lastDirSymbol}${format.anonymous}")
        }
    }

    private fun List<File>.listAsFiles(sb: StringBuilder, prefix: String) {
        var nrOfItems: Int
        var appendAnonymous: Boolean
        filter { it.shallDisplay() }
            .also {
                nrOfItems = min(it.size, args.limitFilesTo)
                appendAnonymous = nrOfItems < it.size
            }
            .take(nrOfItems)
            .forEach { sb.appendLine().append("$prefix${it.name}") }
        if (appendAnonymous) {
            sb.appendLine().append("$prefix${format.anonymous}")
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
