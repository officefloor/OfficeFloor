package net.officefloor.frame.api.build;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedFunctionContainer;

/**
 * Builds a {@link Flow} from a {@link ManagedFunctionContainer} or
 * {@link ManagedObjectSource}.
 *
 * @author Daniel Sagenschneider
 */
public interface FlowBuilder<F extends Enum<F>> extends FunctionBuilder<F> {

	/**
	 * Specifies the next {@link ManagedFunction} to be executed.
	 * 
	 * @param functionName
	 *            Name of the next {@link ManagedFunction}.
	 * @param argumentType
	 *            Type of argument passed to the next {@link ManagedFunction}.
	 *            May be <code>null</code> to indicate no argument.
	 */
	void setNextFunction(String functionName, Class<?> argumentType);

}