package net.officefloor.tutorial.r2dbchttpserver;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.r2dbc.R2dbcSource;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;
import reactor.core.publisher.Mono;

/**
 * R2DBC logic.
 * 
 * @author Daniel Sagenschneider
 */
public class R2dbcLogic {

	// START SNIPPET: reactor
	public Mono<Message> retrieveData(@HttpPathParameter("id") String id, R2dbcSource source) {
		return source.getConnection()
				.flatMap(connection -> Mono.from(connection.createStatement("SELECT CONTENT FROM MESSAGE WHERE ID = $1")
						.bind(0, Integer.parseInt(id)).execute()))
				.flatMap(result -> Mono.from(result.map((row, metadata) -> {
					String content = row.get(0, String.class);
					return new Message(content);
				})));
	}
	// END SNIPPET: reactor

	// START SNIPPET: send
	public void send(@Parameter Message message, ObjectResponse<Message> response) {
		response.send(message);
	}
	// END SNIPPET: send
}
