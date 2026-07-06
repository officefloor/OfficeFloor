package net.officefloor.tutorial.springrestgovernance;

import java.util.ArrayList;
import java.util.List;

// START SNIPPET: tutorial
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
