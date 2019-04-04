package net.officefloor.tutorial.kotlinhttpserver

import net.officefloor.web.HttpObject

@HttpObject
data class KotlinRequest(val name: String)