package net.officefloor.plugin.clazz;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.internal.structure.Flow;

/**
 * {@link FlowCallback} that propagates failures and only handles success.
 * 
 * @author Daniel Sagenschneider
 */
@FunctionalInterface
public interface FlowSuccessful extends FlowCallback {

	/**
	 * Default implementation of {@link FlowCallback} to escalate and then invoke
	 * successful handling.
	 */
	default void run(Throwable escalation) throws Throwable {

		// Ensure propagate flow failure
		if (escalation != null) {
			throw escalation;
		}

		// Successful flow
		this.run();
	}

	/**
	 * Invoked on completion of successful {@link Flow}.
	 * 
	 * @throws Throwable Possible failure in handling completion.
	 */
	void run() throws Throwable;
}