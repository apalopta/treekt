package de.apalopta.treekt

import java.io.File
import kotlin.math.min

/** Creates the directory tree output. */
class FileLister(private val args: Arguments) {
    private val format = args.format
    private val dir = args.dir

    fun display() {
        println(list())
    }

    /** Returns the directory listing as String. */
    fun list(): String = StringBuilder()
        .append(dir.path)
        .append(listDir(dir, 1))
        .toString()


    private fun listDir(dir: File, level: Int, prefix: String = ""): String {
        val sb = StringBuilder()

        if (level <= args.levels) {
            val (dirs, files) = dir.listFiles().partition { it.isDirectory }

            showDirs(dirs, sb, prefix, level)
            if (args.showFiles) {
                showFiles(files, sb, prefix)
            }
        }

        return sb.toString()
    }

    private fun String.toCurrentDirPrefix(isLastDisplayed: Boolean) =
        if (isLastDisplayed) "$this${format.lastDirSymbol}" else "$this${format.runningDirSymbol}"

    private fun showDirs(dirs: List<File>, sb: StringBuilder, prefix: String, level: Int) {
        var nrOfDirs: Int
        var appendAnonymous: Boolean
        var lastDisplayedIndex: Int
        dirs.filter { it.wouldDisplay() }
            .also {
                nrOfDirs = min(it.size, args.limitDirsTo)
                appendAnonymous = it.size > args.limitDirsTo
                lastDisplayedIndex = if (appendAnonymous) nrOfDirs else nrOfDirs - 1
            }.take(nrOfDirs).onEachIndexed { index, currentDir ->
                val thisLinesPrefix = prefix.toCurrentDirPrefix(index == lastDisplayedIndex)
                sb.appendLine().append("$thisLinesPrefix${currentDir.name}")
                sb.append(listDir(currentDir, level + 1, format.prefixForNextLevel(thisLinesPrefix)))
            }.also {
                if (appendAnonymous) {
                    sb.appendLine().append("$prefix${format.lastDirSymbol}${format.anonymous}")
                }
            }
    }

    private fun showFiles(files: List<File>, sb: StringBuilder, prefix: String) {
        var nrOfFiles: Int
        var appendAnonymous: Boolean
        files.filter { it.wouldDisplay() }
            .also {
                nrOfFiles = min(it.size, args.limitFilesTo)
                appendAnonymous = it.size > args.limitFilesTo
            }
            .take(nrOfFiles)
            .forEach { sb.appendLine().append("$prefix${it.name}") }
            .also {
                if (appendAnonymous) {
                    sb.appendLine().append("$prefix${format.anonymous}")
                }
            }
    }

    private fun File.wouldDisplay() = dontHide() && dontSkip() && dontSkipInGeneral()

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
