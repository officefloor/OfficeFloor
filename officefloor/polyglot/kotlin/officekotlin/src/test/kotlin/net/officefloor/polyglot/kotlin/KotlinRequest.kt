package net.officefloor.polyglot.kotlin

import net.officefloor.web.HttpObject

/**
 * Kotlin request.
 */
@HttpObject
data class KotlinRequest(val id: Int, val message: String)