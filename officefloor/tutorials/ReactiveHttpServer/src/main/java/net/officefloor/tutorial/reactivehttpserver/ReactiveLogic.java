package net.officefloor.tutorial.reactivehttpserver;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.spring.reactive.ReactiveWoof;
import net.officefloor.web.ObjectResponse;

/**
 * Reactive logic.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class ReactiveLogic {

	public void reactive(WebClient client, AsynchronousFlow flow, ObjectResponse<ServerResponse> response) {
		client.get().uri("http://localhost:7878/server").accept(MediaType.APPLICATION_JSON).retrieve()
				.bodyToMono(ServerResponse.class)
				.subscribe(ReactiveWoof.send(flow, response), ReactiveWoof.propagateHttpError(flow));
	}
}
// END SNIPPET: tutorial