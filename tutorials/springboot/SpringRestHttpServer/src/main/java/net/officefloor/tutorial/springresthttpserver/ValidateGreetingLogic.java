package net.officefloor.tutorial.springresthttpserver;

import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

// START SNIPPET: tutorial
public class ValidateGreetingLogic {

	@FunctionalInterface
	public interface ValidGreetingFlow {
		void flow(GreetingRequest request);
	}

	public void service(
			@RequestBody GreetingRequest request,
			@Flow("valid") ValidGreetingFlow valid,
			ObjectResponse<GreetingResponse> response) {
		if (request.getName() == null || request.getName().isBlank()) {
			response.send(new GreetingResponse("Hello, World!"));
		} else {
			valid.flow(request);
		}
	}
}
// END SNIPPET: tutorial
