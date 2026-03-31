package net.officefloor.tutorial.springresthttpserver;

import net.officefloor.plugin.section.clazz.Parameter;

// START SNIPPET: tutorial
public class PostGreetingLogic {

	public GreetingResponse service(
			@Parameter GreetingRequest request,
			GreetingService greetingService) {
		return new GreetingResponse(greetingService.greet(request.getName()));
	}
}
// END SNIPPET: tutorial
