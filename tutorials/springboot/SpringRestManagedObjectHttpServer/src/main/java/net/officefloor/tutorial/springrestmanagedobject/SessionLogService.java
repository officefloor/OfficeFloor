package net.officefloor.tutorial.springrestmanagedobject;

import net.officefloor.plugin.section.clazz.Parameter;

// START SNIPPET: tutorial
public class SessionLogService {

	public void log(@Parameter String sessionId) {
		// In production: write to audit log, metrics, distributed trace, etc.
		// The source triggers this function via ManagedObjectExecuteContext,
		// passing the session ID as the typed flow argument.
	}
}
// END SNIPPET: tutorial
