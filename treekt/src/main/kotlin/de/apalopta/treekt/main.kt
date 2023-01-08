package de.apalopta.treekt

/** The treekt main method. */
fun main(args: Array<String>) {

    val arguments = Arguments(args)
//    val contentCreator = ContentCreator(arguments)
    val contentCreator = FileLister(arguments)

    // write to file - if given - or just display
    if (arguments.out != null) {
        arguments.out.run {
            parentFile.mkdirs()
            writeText(contentCreator.listing())
        }
    } else {
        contentCreator.display()
    }
}
