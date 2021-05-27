package de.apalopta.treekt

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.multiple
import java.io.File
import java.nio.file.Paths

class Arguments(private val args: Array<String>) {
    val levels: Int
    val dir: File
    val hideSystemDirs: Boolean
    val hideSystemFiles: Boolean
    val skip : Regex?
    val skipDirectories: List<String>
    val skipFiles: List<String>
//    val truncateTo: Int
    val out: File?

    val parser: ArgParser = ArgParser("treekt")

    init {
        val depth by parser.option(ArgType.Int, shortName = "l", description = "number of levels (max. 256)").default(ContentCreator.MAX_DEPTH)
        val dir by parser.option(ArgType.String, shortName = "d", description = "directory").default(Paths.get("").toAbsolutePath().toString())
        val skip by parser.option(ArgType.String, shortName = "s", description = "skip pattern")
        val skipDir by parser.option(ArgType.String, shortName = "sd", description = "skip directory").multiple()
        val skipFile by parser.option(ArgType.String, shortName = "sf", description = "skip file").multiple()
        val hideSystemDirs by parser.option(ArgType.Boolean, shortName = "hsd", description = "hide system directories").default(false)
        val hideSystemFiles by parser.option(ArgType.Boolean, shortName = "hsf", description = "hide system files").default(false)
//        val truncate by parser.option(ArgType.Int, shortName = "t", description = "truncate the number of displayed directories/files").default(Int.MAX_VALUE)
        val out by parser.option(ArgType.String, shortName = "o", description = "output file")

        parser.parse(args)

        this.levels = depth.coerceAtMost(ContentCreator.MAX_DEPTH).coerceAtLeast(1)
        this.dir = File(dir)
        this.skip = if (skip != null) """$skip""".toRegex() else null
        this.skipDirectories = skipDir.map { it.replace('\\', '/') }
        this.skipFiles = skipFile.map { it.replace('\\', '/') }
        this.hideSystemDirs = hideSystemDirs
        this.hideSystemFiles = hideSystemFiles
//        this.truncateTo = truncate
        this.out = if (out != null) File(out!!) else null
    }

    override fun toString(): String {
        return parser.toString()
//        return mapOf(
//            "depth" to levels.toString(),
//            "dir" to dir.toString(),
//            "hideSystemDirs" to hideSystemDirs.toString(),
//            "hideSystemFiles" to hideSystemFiles.toString(),
//            "skip (regex)" to skip.toString(),
//            "skipDir" to skipDirectories.toString(),
//            "skipFile" to skipFiles.toString(),
//            "out" to out.toString()
//        ).map { "${it.key}: ${it.value}" }.joinToString(", ", prefix = "[", postfix = "]")
    }
}
