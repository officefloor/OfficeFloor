package net.officefloor.tutorial.springrestgovernance;

import net.officefloor.web.ObjectResponse;

import java.util.List;

// START SNIPPET: tutorial
public class GovernedService {

	public void service(AuditRecord record, ObjectResponse<List<String>> response) {
		record.recordEvent("service-called");
		response.send(record.getEvents());
	}
}
// END SNIPPET: tutorial
