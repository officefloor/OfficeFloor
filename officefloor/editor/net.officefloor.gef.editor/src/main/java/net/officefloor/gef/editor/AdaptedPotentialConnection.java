package net.officefloor.gef.editor;

import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;

/**
 * Potential {@link AdaptedConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedPotentialConnection {

	/**
	 * Obtains the source {@link Model} {@link Class}.
	 * 
	 * @return Source {@link Model} {@link Class}.
	 */
	Class<?> getSourceModelClass();

	/**
	 * Obtains the target {@link Model} {@link Class}.
	 * 
	 * @return Target {@link Model} {@link Class}.
	 */
	Class<?> getTargetModelClass();

	/**
	 * Indicates whether can create the {@link ConnectionModel}.
	 * 
	 * @return <code>true</code> if able to create the {@link ConnectionModel}.
	 */
	boolean canCreateConnection();

}