package de.apalopta.treekt

// plopt

//import de.apalopta.treekt.utilities.StringUtils
//import org.apache.commons.text.WordUtils

fun main(args: Array<String>) {

//    val tokens = StringUtils.split(MessageUtils.getMessage())
//    val result = StringUtils.join(tokens)
//    println(WordUtils.capitalize(result))


    val arguments = Arguments(args)

    val contentCreator = ContentCreator(arguments)
    if (arguments.out != null) {
        arguments.out.parentFile.mkdirs()
        arguments.out.writeText(contentCreator.list())
    } else {
        contentCreator.display()
    }
}
