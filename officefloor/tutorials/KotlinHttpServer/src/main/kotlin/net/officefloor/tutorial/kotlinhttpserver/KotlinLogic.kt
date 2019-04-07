package net.officefloor.tutorial.kotlinhttpserver

import net.officefloor.web.ObjectResponse

/**
 * Services request in Kotlin.
 */
fun service(request: KotlinRequest, response: ObjectResponse<KotlinResponse>) {
	response.send(KotlinResponse("Hello ${request.name} from Kotlin"))
}
