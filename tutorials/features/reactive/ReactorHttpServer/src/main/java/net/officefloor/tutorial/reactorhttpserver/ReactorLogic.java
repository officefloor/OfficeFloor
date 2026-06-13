package net.officefloor.tutorial.reactorhttpserver;

import org.springframework.web.reactive.function.client.WebClient;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import reactor.core.publisher.Mono;

public class ReactorLogic {

	// START SNIPPET: reactor
	public Mono<ServerResponse> reactive(WebClient client) {
		return client.get().uri("http://localhost:7878/server").accept(MediaType.APPLICATION_JSON).retrieve()
				.bodyToMono(ServerResponse.class);
	}
	// END SNIPPET: reactor
	
	// START SNIPPET: send
	public void send(@Parameter ServerResponse result, ObjectResponse<ServerResponse> response) {
		response.send(result);
	}
	// END SNIPPET: send
}
