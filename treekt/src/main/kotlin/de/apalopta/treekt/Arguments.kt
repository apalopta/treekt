package de.apalopta.treekt

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.multiple
import java.io.File
import java.nio.file.Paths

enum class HideType() {
    NONE,
    ALL_SYSTEM_FILES,
    SYSTEM_FILES_FILES_ONLY,
    SYSTEM_FILES_DIRECTORIES_ONLY
}

enum class Format() {
    ASCII
//    UTF8
}

/** CLI for treekt. */
class Arguments(args: Array<String>) {
    val levels: Int
    val dir: File
    val showFiles: Boolean
    val hideType: HideType
    val skip: List<Regex>
    val skipDirectories: List<String>
    val skipFiles: List<String>
    val limitDirsTo: Int
    val limitFilesTo: Int
    val out: File?
    val format: Format

    private val parser = ArgParser("treekt")

    init {
        val depth by parser.option(ArgType.Int, shortName = "l", description = "number of levels (max. ${FileLister.MAX_DEPTH})").default(FileLister.MAX_DEPTH)
        val dir by parser.option(ArgType.String, shortName = "d", description = "directory").default(Paths.get("").toAbsolutePath().toString())
        val showFiles by parser.option(ArgType.Boolean, shortName = "f", description = "show files").default(false)
        val skip by parser.option(ArgType.String, shortName = "s", description = "skip pattern (works on directories and files)").multiple().default(listOf(".*"))
        val skipDir by parser.option(ArgType.String, shortName = "sd", description = "skip directory pattern").multiple()
        val skipFile by parser.option(ArgType.String, shortName = "sf", description = "skip file pattern").multiple()
        val hideFiles by parser.option(ArgType.Choice<HideType>(), shortName = "hf", description = "hide system files of type (not all starting with '.' are hidden files!").default(HideType.NONE)
        val limitDirsTo by parser.option(ArgType.Int, shortName = "ld", description = "limit the number of displayed directories").default(Int.MAX_VALUE)
        val limitFilesTo by parser.option(ArgType.Int, shortName = "lf", description = "limit the number of displayed files").default(Int.MAX_VALUE)
        val out by parser.option(ArgType.String, shortName = "o", description = "output file")
        val format by parser.option(ArgType.Choice<Format>(), description = "output format").default(Format.ASCII)

        parser.parse(args)

        this.levels = depth.coerceIn(0..FileLister.MAX_DEPTH)
        this.dir = File(dir)
        this.showFiles = showFiles

        this.skip = skip.map { it.toRegex() }
        this.skipDirectories = skipDir.map { it.replace('\\', '/') }
        this.skipFiles = skipFile.map { it.replace('\\', '/') }

        this.hideType = hideFiles

        this.limitDirsTo = limitDirsTo
        this.limitFilesTo = limitFilesTo

        val outFilePath = if (out != null) Paths.get(out!!) else null
        this.out = outFilePath?.toAbsolutePath()?.toFile()

        this.format = format
    }
}
