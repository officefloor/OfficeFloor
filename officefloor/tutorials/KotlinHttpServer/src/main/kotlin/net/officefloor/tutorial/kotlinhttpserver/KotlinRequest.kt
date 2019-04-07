package net.officefloor.tutorial.kotlinhttpserver

import net.officefloor.web.HttpObject

/**
 * Kotlin request.
 */
@HttpObject
data class KotlinRequest(val name: String)