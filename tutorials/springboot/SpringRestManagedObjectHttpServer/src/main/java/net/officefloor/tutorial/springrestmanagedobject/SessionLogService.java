package net.officefloor.tutorial.springrestmanagedobject;

import net.officefloor.plugin.section.clazz.Parameter;

// START SNIPPET: tutorial
/**
 * Handler for the {@link SessionIdSource} {@code Flows.LOG} output.
 *
 * <p>OfficeFloor requires every flow declared via
 * {@link net.officefloor.frame.api.managedobject.source.ManagedObjectSource.MetaDataContext#addFlow}
 * to be wired to a handler in the YAML via {@code outputs:}.  The {@link Parameter}
 * annotation receives the typed argument passed when the source triggers the flow via
 * {@link net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext}.
 */
public class SessionLogService {

	public void log(@Parameter String sessionId) {
		// In production: write to audit log, metrics, distributed trace, etc.
		// The source triggers this function via ManagedObjectExecuteContext,
		// passing the session ID as the typed flow argument.
	}
}
// END SNIPPET: tutorial
