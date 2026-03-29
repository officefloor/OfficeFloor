package net.officefloor.tutorial.kotlinhttpserver

import net.officefloor.web.HttpObject

/**
 * Kotlin request.
 */
// START SNIPPET: tutorial
@HttpObject
data class KotlinRequest(val name: String)
// END SNIPPET: tutorial