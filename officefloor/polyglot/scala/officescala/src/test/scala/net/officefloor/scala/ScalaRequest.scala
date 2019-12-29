package net.officefloor.scala

import net.officefloor.web.HttpObject

@HttpObject
class ScalaRequest(val identifier: Int, val message: String)