package net.officefloor.tutorial.springrestgovernance;

import java.util.ArrayList;
import java.util.List;

// START SNIPPET: tutorial
/**
 * PROCESS-scoped managed object: one fresh instance per request, shared across
 * all functions within that request.  Implements {@link Auditable} so the
 * {@code audit} governance can enroll it automatically via auto-wire extensions.
 */
public class AuditRecord implements Auditable {

	private final List<String> events = new ArrayList<>();

	@Override
	public void recordEvent(String event) {
		events.add(event);
	}

	public List<String> getEvents() {
		return new ArrayList<>(events);
	}
}
// END SNIPPET: tutorial
