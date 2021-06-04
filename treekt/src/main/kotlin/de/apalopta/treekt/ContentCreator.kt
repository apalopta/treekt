package de.apalopta.treekt

import java.io.File
import kotlin.math.min

/** Creates the directory tree output. */
class ContentCreator(private val args: Arguments) {

    companion object {
        const val MAX_DEPTH = 256
    }

    fun display() {
        println("----")
        println(list())
        println("----")
    }

    fun list(): String = StringBuilder(args.dir.path).append(listFiles(args.dir, 1)).toString()

    private fun listFiles(dir: File, level: Int, prefix: String = ""): String {
        val sb = StringBuilder()

        val filesOfDirectory = dir.listFiles()
        if (filesOfDirectory != null && filesOfDirectory.isNotEmpty()) {
            val (dirs, files) = filesOfDirectory.partition { it.isDirectory }
            displayDirs(dirs.filter { wouldDisplayDir(it) }, sb, prefix, level)
            displayFiles(files.filter { wouldDisplayFile(it) }, sb, prefix)
        }

        return sb.toString()
    }

    private fun displayDirs(displayableDirs: List<File>, sb: StringBuilder, prefix: String, level: Int) {
        val numberOfDirs = Math.min(displayableDirs.size, args.limitDirsTo)
        val appendAnonymousDir = displayableDirs.size > args.limitDirsTo
        val lastIndex = if (appendAnonymousDir) numberOfDirs else (numberOfDirs - 1)

        displayableDirs.take(numberOfDirs).forEachIndexed { index, currentDir ->
            val thisLinesPrefix = if (index == lastIndex) "$prefix\\---" else "$prefix+---"
            sb.appendLine().append("$thisLinesPrefix${currentDir.name}")
            if (level < args.levels) {
                sb.append(
                    listFiles(currentDir, level + 1, thisLinesPrefix.replace(Regex("""[\\-]"""), " ").replace('+', '|'))
                )
            }
        }
        if (appendAnonymousDir) {
            sb.appendLine().append("$prefix\\---...")
        }
    }

    private fun displayFiles(displayableFiles: List<File>, sb: StringBuilder, prefix: String) {
        val appendAnonymousFile = displayableFiles.size > args.limitFilesTo

        displayableFiles.take(min(displayableFiles.size, args.limitFilesTo)).forEach { file ->
            sb.appendLine().append("$prefix${file.name}")
        }
        if (appendAnonymousFile) {
            sb.appendLine().append("$prefix...")
        }
    }

    private fun wouldDisplayFile(file: File) = doNotHideFile(file) && doNotSkipFile(file) && doNotMatchFile(file)

    private fun doNotSkipFile(file: File) = args.skipFiles.isEmpty() || args.skipFiles.none { file.toURI().toString().contains(it) }

    private fun doNotHideFile(file: File) = !(args.hideSystemFiles && file.name.startsWith('.'))

    private fun doNotMatchFile(file: File) =
        args.skip == null || !args.skip.containsMatchIn(file.toURI().toString()) || !args.skip.containsMatchIn(file.toString())

    private fun wouldDisplayDir(dir: File) = doNotHideDir(dir) && doNotSkipDir(dir) && doNotMatchFile(dir)

    private fun doNotSkipDir(dir: File) = args.skipDirectories.isEmpty() || args.skipDirectories.none { dir.toURI().toString().contains(it) }

    private fun doNotHideDir(dir: File) = !(args.hideSystemDirs && dir.name.startsWith('.'))
}
