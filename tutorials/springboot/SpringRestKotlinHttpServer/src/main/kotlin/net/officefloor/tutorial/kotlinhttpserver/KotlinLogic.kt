package net.officefloor.tutorial.kotlinhttpserver

import net.officefloor.web.ObjectResponse
import org.springframework.web.bind.annotation.RequestBody

fun service(@RequestBody request: KotlinRequest, response: ObjectResponse<KotlinResponse>) {
	response.send(KotlinResponse("Hello ${request.name} from Kotlin"))
}