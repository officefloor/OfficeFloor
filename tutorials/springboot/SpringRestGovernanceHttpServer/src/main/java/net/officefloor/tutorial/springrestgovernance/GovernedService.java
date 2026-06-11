package net.officefloor.tutorial.springrestgovernance;

import net.officefloor.web.ObjectResponse;

import java.util.List;

// START SNIPPET: tutorial
/**
 * REST service decorated with {@code govern: [ audit ]} in its REST YAML.
 *
 * By the time this method body executes, {@link AuditGovernance#govern} has
 * already been called and {@code "governance-begin"} is in the event list.
 * After this method returns, {@link AuditGovernance#enforce} is called and
 * {@code "governance-commit"} is appended (not visible in the response because
 * the response is sent before enforcement runs).
 */
public class GovernedService {

	public void service(AuditRecord record, ObjectResponse<List<String>> response) {
		record.recordEvent("service-called");
		response.send(record.getEvents());
	}
}
// END SNIPPET: tutorial
