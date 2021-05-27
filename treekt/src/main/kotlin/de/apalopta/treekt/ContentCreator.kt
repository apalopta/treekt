package de.apalopta.treekt

import java.io.File

class ContentCreator(private val args: Arguments) {

    companion object {
        const val MAX_DEPTH = 256
    }

    fun display() {
        println("dir:             ${args.dir.path}")
        println("levels:          ${args.levels}")
        println("skipDirectories: ${args.skipDirectories.joinToString(", ")}")
        println("hideSystemDirs:  ${args.hideSystemDirs}")
        println("hideSystemFiles: ${args.hideSystemFiles}")

        println("----")
        println(args.toString())
        println(list())
        println("----")
    }

    fun list(): String = StringBuilder(args.dir.path).append(listFiles(args.dir, 1)).toString()

    private fun listFiles(dir: File, level: Int, prefix: String = ""): String {
        val sb = StringBuilder()

        val filesOfDirectory = dir.listFiles()
        if (filesOfDirectory != null && filesOfDirectory.isNotEmpty()) {
            val (dirs, files) = filesOfDirectory.partition { it.isDirectory }
            val lastIndex = dirs.size - 1
            // TODO:2021-05-26:anja: handle truncateLargeDirectories
            dirs.forEachIndexed { index, currentDir ->
                if (showDir(currentDir)) {
                    val thisLinesPrefix = if (index == lastIndex) "$prefix\\---" else "$prefix+---"
                    sb.appendLine().append("$thisLinesPrefix${currentDir.name}")
                    if (level < args.levels) {
                        sb.append(
                            listFiles(currentDir, level + 1, thisLinesPrefix.replace(Regex("""[\\-]"""), " ").replace('+', '|'))
                        )
                    }
                }
            }

            files.forEach { file ->
                if (showFile(file)) {
                    sb.appendLine().append("$prefix${file.name}")
                }
            }
        }

        return sb.toString()
    }

    private fun showFile(file: File) = doNotHideFile(file) && doNotSkipFile(file) && doNotMatchFile(file)

    private fun doNotSkipFile(file: File) = args.skipFiles.isEmpty() || args.skipFiles.none { file.toURI().toString().contains(it) }

    private fun doNotHideFile(file: File) = !(args.hideSystemFiles && file.name.startsWith('.'))

    private fun doNotMatchFile(file: File) =
        (args.skip == null)
            || !args.skip.containsMatchIn(file.toURI().toString())
                || !args.skip.containsMatchIn(file.toString())

    private fun showDir(dir: File) = doNotHideDir(dir) && doNotSkipDir(dir) && doNotMatchFile(dir)

    private fun doNotSkipDir(dir: File) = args.skipDirectories.isEmpty() || args.skipDirectories.none { dir.toURI().toString().contains(it) }

    private fun doNotHideDir(dir: File) = !(args.hideSystemDirs && dir.name.startsWith('.'))
}
