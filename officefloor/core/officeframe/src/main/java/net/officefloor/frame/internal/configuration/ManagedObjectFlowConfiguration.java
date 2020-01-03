package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Configuration of a {@link Flow} instigated by a {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectFlowConfiguration<F extends Enum<F>> {

	/**
	 * Obtains the name to identify this {@link Flow}.
	 * 
	 * @return Name identifying this {@link Flow}.
	 */
	String getFlowName();

	/**
	 * Obtains the key for this {@link Flow}.
	 * 
	 * @return Key for this flow. May be <code>null</code> if {@link Flow}
	 *         instances are {@link Indexed}.
	 */
	F getFlowKey();

	/**
	 * Obtains the {@link ManagedFunctionReference} for this {@link Flow}.
	 * 
	 * @return {@link ManagedFunctionReference} to the {@link Flow}.
	 */
	ManagedFunctionReference getManagedFunctionReference();

}