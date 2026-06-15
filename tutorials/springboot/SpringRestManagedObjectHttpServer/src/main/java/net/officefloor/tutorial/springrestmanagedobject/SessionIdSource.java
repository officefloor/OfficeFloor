package net.officefloor.tutorial.springrestmanagedobject;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;

// START SNIPPET: tutorial
public class SessionIdSource extends AbstractManagedObjectSource<None, SessionIdSource.Flows> {

	public enum Flows {
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
