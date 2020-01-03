package net.officefloor.script;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Provides means for script to invoke {@link Flow}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ScriptFlow {

	/**
	 * Undertakes the {@link Flow}.
	 * 
	 * @param parameter Parameter. May be <code>null</code>.
	 * @param callback  {@link FlowCallback}.
	 */
	void doFlow(Object parameter, FlowCallback callback);

}