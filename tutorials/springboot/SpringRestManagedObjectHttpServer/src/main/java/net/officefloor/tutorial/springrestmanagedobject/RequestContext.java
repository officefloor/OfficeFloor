package net.officefloor.tutorial.springrestmanagedobject;

import java.time.Instant;
import java.util.UUID;

// START SNIPPET: tutorial
public class RequestContext {

	private final String correlationId = UUID.randomUUID().toString();

	private final Instant startTime = Instant.now();

	public String getCorrelationId() {
		return correlationId;
	}

	public Instant getStartTime() {
		return startTime;
	}
}
// END SNIPPET: tutorial
