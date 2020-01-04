package net.officefloor.tutorial.ziohttpserver

import net.officefloor.web.HttpObject

/**
 * Request to ZIO HTTP Server.
 *
 * @param id Identifier of message to return.
 */
@HttpObject
class ZioRequest(val id: Int)