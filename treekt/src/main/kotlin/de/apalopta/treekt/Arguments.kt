package de.apalopta.treekt

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.multiple
import java.io.File
import java.nio.file.Paths

/** CLI for treekt. */
class Arguments(args: Array<String>) {
    val levels: Int
    val dir: File
    val showFiles: Boolean
    val hideSystemDirs: Boolean
    val hideSystemFiles: Boolean
    val skip : Regex?
    val skipDirectories: List<String>
    val skipFiles: List<String>
    val limitDirsTo: Int
    val limitFilesTo: Int
    val out: File?

    private val parser: ArgParser = ArgParser("treekt")

    init {
        val depth by parser.option(ArgType.Int, shortName = "l", description = "number of levels (max. 256)").default(ContentCreator.MAX_DEPTH)
        val dir by parser.option(ArgType.String, shortName = "d", description = "directory").default(Paths.get("").toAbsolutePath().toString())
        val showFiles by parser.option(ArgType.Boolean, shortName = "f", description = "show files").default(false)
        val skip by parser.option(ArgType.String, shortName = "s", description = "skip pattern")
        val skipDir by parser.option(ArgType.String, shortName = "sd", description = "skip directory").multiple()
        val skipFile by parser.option(ArgType.String, shortName = "sf", description = "skip file").multiple()
        val hideSystemDirs by parser.option(ArgType.Boolean, shortName = "hsd", description = "hide system directories").default(false)
        val hideSystemFiles by parser.option(ArgType.Boolean, shortName = "hsf", description = "hide system files").default(false)
        val limitDirsTo by parser.option(ArgType.Int, shortName = "td", description = "limit the number of displayed directories").default(Int.MAX_VALUE)
        val limitFilesTo by parser.option(ArgType.Int, shortName = "tf", description = "limit the number of displayed files").default(Int.MAX_VALUE)
        val out by parser.option(ArgType.String, shortName = "o", description = "output file")

        parser.parse(args)

        this.levels = depth.coerceAtMost(ContentCreator.MAX_DEPTH).coerceAtLeast(1)
        this.dir = File(dir)
        this.showFiles = showFiles

        this.skip = if (skip != null) """$skip""".toRegex() else null
        this.skipDirectories = skipDir.map { it.replace('\\', '/') }
        this.skipFiles = skipFile.map { it.replace('\\', '/') }

        this.hideSystemDirs = hideSystemDirs
        this.hideSystemFiles = hideSystemFiles

        this.limitDirsTo = limitDirsTo
        this.limitFilesTo = limitFilesTo

        val outFilePath = if (out != null) Paths.get(out!!) else null
        this.out = outFilePath?.toAbsolutePath()?.toFile()
    }
}
