package net.officefloor.tutorial.springwebfluxhttpserver.migrated;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.tutorial.springwebfluxhttpserver.ResponseModel;
import net.officefloor.web.ObjectResponse;

/**
 * Sends the {@link ResponseModel}.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class SendResponse {

	public void send(@Parameter ResponseModel payload, ObjectResponse<ResponseModel> response) {
		response.send(payload);
	}
}
// END SNIPPET: tutorial