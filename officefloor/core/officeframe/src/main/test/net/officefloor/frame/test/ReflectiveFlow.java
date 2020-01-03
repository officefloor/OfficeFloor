package net.officefloor.frame.test;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Reflective flow to be used as a parameter.
 * 
 * @author Daniel Sagenschneider
 */
public interface ReflectiveFlow {

	/**
	 * Invokes the {@link Flow}.
	 * 
	 * @param parameter
	 *            Parameter to the {@link Flow}.
	 * @param callback
	 *            Optional {@link FlowCallback}. May be <code>null</code>.
	 */
	void doFlow(Object parameter, FlowCallback callback);

}