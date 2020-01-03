package net.officefloor.compile.managedfunction;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.internal.structure.Flow;

/**
 * <code>Type definition</code> of a {@link Flow} possibly instigated by a
 * {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionFlowType<F extends Enum<F>> {

	/**
	 * Obtains the name for the {@link ManagedFunctionFlowType}.
	 * 
	 * @return Name for the {@link ManagedFunctionFlowType}.
	 */
	String getFlowName();

	/**
	 * <p>
	 * Obtains the index for the {@link ManagedFunctionFlowType}.
	 * <p>
	 * Should there be an {@link Enum} then will be the {@link Enum#ordinal()}
	 * value. Otherwise will be the index that this was added.
	 * 
	 * @return Index for the {@link ManagedFunctionFlowType}.
	 */
	int getIndex();

	/**
	 * Obtains the type of the argument passed by the {@link ManagedFunction} to the
	 * {@link Flow}.
	 * 
	 * @return Type of argument passed to {@link Flow}. May be <code>null</code> to
	 *         indicate no argument.
	 */
	Class<?> getArgumentType();

	/**
	 * Obtains the {@link Enum} key for the {@link ManagedFunctionFlowType}.
	 * 
	 * @return {@link Enum} key for the {@link ManagedFunctionFlowType}. May be
	 *         <code>null</code> if no {@link Enum} for flows.
	 */
	F getKey();

}