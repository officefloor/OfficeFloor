package net.officefloor.plugin.clazz.flow;

import java.lang.reflect.Method;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Context for the {@link ClassFlowRegistry}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassFlowContext {

	/**
	 * Obtains the Type declaring the {@link Method} of this flow.
	 * 
	 * @return Type declaring the {@link Method} of this flow.
	 */
	Class<?> getFlowInterfaceType();

	/**
	 * Obtains the {@link Method}.
	 * 
	 * @return {@link Method}.
	 */
	Method getMethod();

	/**
	 * Indicates whether to spawn in {@link ThreadState}.
	 * 
	 * @return <code>true</code> to spawn in {@link ThreadState}.
	 */
	boolean isSpawn();

	/**
	 * Obtains the parameter type for the {@link Flow}. Will be <code>null</code> if
	 * no parameter.
	 * 
	 * @return Parameter type for the {@link Flow}. Will be <code>null</code> if no
	 *         parameter.
	 */
	Class<?> getParameterType();

	/**
	 * Flags if {@link FlowCallback}.
	 * 
	 * @return <code>true</code> if {@link FlowCallback}.
	 */
	boolean isFlowCallback();

}