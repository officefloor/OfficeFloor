package net.officefloor.frame.internal.structure;

import java.util.logging.Logger;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Meta-data for the {@link Administration} of a {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectAdministrationMetaData<E, F extends Enum<F>, G extends Enum<G>> {

	/**
	 * <p>
	 * Obtains the {@link ManagedObjectIndex} instances identifying the
	 * {@link ManagedObject} instances that must be loaded before the
	 * {@link Administration} may be executed.
	 * <p>
	 * The order of the {@link ManagedObjectIndex} instances must be respected as
	 * they are sorted to enable appropriate {@link CoordinatingManagedObject} to
	 * co-ordinate with dependencies.
	 * 
	 * @return Listing of {@link ManagedObjectIndex} instances.
	 */
	ManagedObjectIndex[] getRequiredManagedObjects();

	/**
	 * Obtains the {@link AdministrationMetaData}.
	 * 
	 * @return {@link AdministrationMetaData}.
	 */
	AdministrationMetaData<E, F, G> getAdministrationMetaData();

	/**
	 * Obtains the {@link Logger} for the {@link AdministrationContext}.
	 * 
	 * @return {@link Logger} for the {@link AdministrationContext}.
	 */
	Logger getLogger();

}