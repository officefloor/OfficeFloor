package net.officefloor.frame.api.managedobject.function;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionDependency;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.team.Team;

/**
 * Context for the {@link ManagedObjectFunctionEnhancer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectFunctionEnhancerContext {

	/**
	 * Obtains the name of the {@link ManagedFunction}.
	 * 
	 * @return Name of the {@link ManagedFunction}.
	 */
	String getFunctionName();

	/**
	 * Obtains the {@link ManagedFunctionFactory}.
	 * 
	 * @return {@link ManagedFunctionFactory}.
	 */
	ManagedFunctionFactory<?, ?> getManagedFunctionFactory();

	/**
	 * Indicates if using the {@link ManagedObject} from the
	 * {@link ManagedObjectSource}.
	 * 
	 * @return <code>true</code> if using the {@link ManagedObject} from the
	 *         {@link ManagedObjectSource}.
	 */
	boolean isUsingManagedObject();

	/**
	 * Obtains the {@link ManagedObjectFunctionDependency} instances for the
	 * {@link ManagedFunction}.
	 * 
	 * @return {@link ManagedObjectFunctionDependency} instances for the
	 *         {@link ManagedFunction}.
	 */
	ManagedObjectFunctionDependency[] getFunctionDependencies();

	/**
	 * Obtains the name of the responsible {@link Team}.
	 * 
	 * @return Name of the responsible {@link Team} or <code>null</code> if
	 *         {@link Team} assigned.
	 */
	String getResponsibleTeam();

	/**
	 * Specifies the responsible {@link Team}.
	 * 
	 * @param teamName Name of the responsible {@link Team}.
	 */
	void setResponsibleTeam(String teamName);

}