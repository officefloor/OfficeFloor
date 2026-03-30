package net.officefloor.tutorial.springresthttpserver;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;

// START SNIPPET: tutorial
public class AuditGreetingLogic {

	public void service(
			@Parameter GreetingResponse greeting,
			AuditService auditService,
			ObjectResponse<GreetingResponse> response) {
		auditService.record(greeting.getMessage());
		response.send(greeting);
	}
}
// END SNIPPET: tutorial
