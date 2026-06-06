package net.officefloor.tutorial.springrestmanagedobject;

import net.officefloor.web.ObjectResponse;

// START SNIPPET: tutorial
public class RequestContextService {

	public void service(RequestContext context, ObjectResponse<RequestContextResponse> response) {
		response.send(new RequestContextResponse(
				context.getCorrelationId(),
				context.getStartTime().toEpochMilli()));
	}
}
// END SNIPPET: tutorial
