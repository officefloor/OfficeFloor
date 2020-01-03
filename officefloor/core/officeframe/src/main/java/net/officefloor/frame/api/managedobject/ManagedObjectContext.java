package net.officefloor.frame.api.managedobject;

import java.util.logging.Logger;

/**
 * Context for the {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectContext {

	/**
	 * <p>
	 * Obtains the name this {@link ManagedObject} is bound under.
	 * <p>
	 * This is useful to have a unique name identifying the {@link ManagedObject}.
	 * 
	 * @return Name this {@link ManagedObject} is bound under.
	 */
	String getBoundName();

	/**
	 * Obtains the {@link Logger} for the {@link ManagedObject}.
	 * 
	 * @return {@link Logger} for the {@link ManagedObject}.
	 */
	Logger getLogger();

	/**
	 * Undertakes a {@link ProcessSafeOperation}.
	 * 
	 * @param <R>       Return type from operation
	 * @param <T>       Possible {@link Throwable} type from operation.
	 * @param operation {@link ProcessSafeOperation}.
	 * @return Return value.
	 * @throws T Possible {@link Throwable}.
	 */
	<R, T extends Throwable> R run(ProcessSafeOperation<R, T> operation) throws T;

}