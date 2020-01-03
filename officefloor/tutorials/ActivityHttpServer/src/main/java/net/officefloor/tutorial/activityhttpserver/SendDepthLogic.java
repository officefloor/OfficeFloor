package net.officefloor.tutorial.activityhttpserver;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;

/**
 * Logic to send the {@link Depth}.
 * 
 * @author Daniel Sagenschneider
 */
public class SendDepthLogic {

	// START SNIPPET: tutorial
	public void send(@Parameter Depth depth, ObjectResponse<Depth> response) {
		response.send(depth);
	}
	// END SNIPPET: tutorial
}