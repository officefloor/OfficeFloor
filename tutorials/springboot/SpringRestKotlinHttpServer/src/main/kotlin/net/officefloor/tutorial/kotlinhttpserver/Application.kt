package net.officefloor.tutorial.kotlinhttpserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

// START SNIPPET: tutorial
@SpringBootApplication
open class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
// END SNIPPET: tutorial
