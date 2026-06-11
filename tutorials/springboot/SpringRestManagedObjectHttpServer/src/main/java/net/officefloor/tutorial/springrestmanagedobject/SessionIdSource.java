package net.officefloor.tutorial.springrestmanagedobject;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;

// START SNIPPET: tutorial
/**
 * Custom {@link net.officefloor.frame.api.managedobject.source.ManagedObjectSource}
 * that creates a {@link SessionId} for each request.
 *
 * <p>The {@code Flows.LOG} output demonstrates how a source declares a named flow
 * that OfficeFloor wires to another function in the pipeline via
 * {@code outputs: LOG: logService} in the YAML.  The managed object triggers the
 * flow via {@link net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext},
 * passing a typed argument and receiving a completion callback without blocking any thread.
 *
 * <p>{@link None} is used for the dependency keys parameter because this source
 * has no dependencies on other managed objects.
 */
public class SessionIdSource extends AbstractManagedObjectSource<None, SessionIdSource.Flows> {

	public enum Flows {
		/** Triggered to log the session ID after the request is serviced. */
		LOG
	}

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, Flows> context) throws Exception {
		// Declare the type this source provides
		context.setObjectClass(SessionId.class);

		// Declare the LOG flow: argument is the session ID string to be logged.
		// Wire this flow via  outputs: LOG: <function-name>  in the YAML to connect
		// it to a downstream function.  The source triggers it via
		// ManagedObjectExecuteContext.invokeStartupProcess() or the managed object
		// signals it via ManagedObjectUser.setManagedObject() for async completion.
		context.addFlow(Flows.LOG, String.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		SessionId sessionId = new SessionId();
		return () -> sessionId;
	}
}
// END SNIPPET: tutorial
