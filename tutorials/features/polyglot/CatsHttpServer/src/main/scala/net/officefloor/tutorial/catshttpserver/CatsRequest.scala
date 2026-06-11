package net.officefloor.tutorial.catshttpserver

import net.officefloor.web.HttpObject

/**
 * Request to Cats HTTP server.
 *
 * @param id Identiier of message to return.
 */
@HttpObject
case class CatsRequest(id: Int)