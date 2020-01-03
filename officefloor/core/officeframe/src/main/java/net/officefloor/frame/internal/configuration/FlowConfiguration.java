package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Configuration for a {@link Flow}.
 * 
 * @author Daniel Sagenschneider
 */
public interface FlowConfiguration<F extends Enum<F>> {

	/**
	 * Obtains the name of this {@link Flow}.
	 * 
	 * @return Name of this {@link Flow}.
	 */
	String getFlowName();

	/**
	 * Obtains the reference to the initial {@link ManagedFunction} of this
	 * {@link Flow}.
	 * 
	 * @return Reference to the initial {@link ManagedFunction} of this
	 *         {@link Flow}.
	 */
	ManagedFunctionReference getInitialFunction();

	/**
	 * Indicates whether to spawn a {@link ThreadState} for the {@link Flow}.
	 * 
	 * @return <code>true</code> to spawn a {@link ThreadState} for the
	 *         {@link Flow}.
	 */
	boolean isSpawnThreadState();

	/**
	 * Obtains the index identifying this {@link Flow}.
	 * 
	 * @return Index identifying this {@link Flow}.
	 */
	int getIndex();

	/**
	 * Obtains the key identifying this {@link Flow}.
	 * 
	 * @return Key identifying this {@link Flow}. <code>null</code> if indexed.
	 */
	F getKey();

}