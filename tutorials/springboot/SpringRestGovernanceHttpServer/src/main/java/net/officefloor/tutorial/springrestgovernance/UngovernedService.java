package net.officefloor.tutorial.springrestgovernance;

import net.officefloor.web.ObjectResponse;

import java.util.List;

// START SNIPPET: tutorial
/**
 * REST service with no {@code govern:} declaration in its REST YAML.
 *
 * {@link AuditGovernance} is never activated for this function, so no
 * governance events appear in the record even though {@link AuditRecord}
 * implements {@link Auditable}.
 */
public class UngovernedService {

	public void service(AuditRecord record, ObjectResponse<List<String>> response) {
		record.recordEvent("service-called");
		response.send(record.getEvents());
	}
}
// END SNIPPET: tutorial
