package net.officefloor.frame.internal.structure;

import java.util.logging.Logger;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.api.function.ManagedFunction;

/**
 * Meta-data for the {@link Administration} of the {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionAdministrationMetaData<E, F extends Enum<F>, G extends Enum<G>> {

	/**
	 * Obtains the {@link Logger} for the {@link AdministrationContext}.
	 * 
	 * @return {@link Logger} for the {@link AdministrationContext}.
	 */
	Logger getLogger();

	/**
	 * Obtains the {@link AdministrationMetaData}.
	 * 
	 * @return {@link AdministrationMetaData}.
	 */
	AdministrationMetaData<E, F, G> getAdministrationMetaData();

}