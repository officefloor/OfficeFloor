package net.officefloor.web.resource.source;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.web.resource.HttpFile;
import net.officefloor.web.resource.HttpResourceStore;

/**
 * {@link ManagedFunction} to send the {@link HttpFile} from the
 * {@link HttpResourceStore}.
 * 
 * @author Daniel Sagenschneider
 */
public class TriggerSendHttpFileFunction extends StaticManagedFunction<None, None> {

	/**
	 * {@link HttpPath}.
	 */
	private final HttpPath path;

	/**
	 * Instantiate.
	 * 
	 * @param path Path.
	 */
	public TriggerSendHttpFileFunction(String path) {
		this.path = new HttpPath(path);
	}

	/*
	 * ==================== ManagedFunction ==================
	 */

	@Override
	public void execute(ManagedFunctionContext<None, None> context) throws Exception {
		// Trigger send file
		context.setNextFunctionArgument(this.path);
	}

}