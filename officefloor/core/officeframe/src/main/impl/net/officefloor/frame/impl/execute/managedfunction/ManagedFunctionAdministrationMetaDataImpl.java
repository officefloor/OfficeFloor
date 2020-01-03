package net.officefloor.frame.impl.execute.managedfunction;

import java.util.logging.Logger;

import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.internal.structure.AdministrationMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionAdministrationMetaData;

/**
 * {@link ManagedFunctionAdministrationMetaData} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionAdministrationMetaDataImpl<E, F extends Enum<F>, G extends Enum<G>>
		implements ManagedFunctionAdministrationMetaData<E, F, G> {

	/**
	 * {@link Logger} for {@link AdministrationContext}.
	 */
	private final Logger logger;

	/**
	 * {@link AdministrationMetaData}.
	 */
	private final AdministrationMetaData<E, F, G> administrationMetaData;

	/**
	 * Instantiate.
	 * 
	 * @param logger                 {@link Logger} for
	 *                               {@link AdministrationContext}.
	 * @param administrationMetaData {@link AdministrationMetaData}.
	 */
	public ManagedFunctionAdministrationMetaDataImpl(Logger logger,
			AdministrationMetaData<E, F, G> administrationMetaData) {
		this.logger = logger;
		this.administrationMetaData = administrationMetaData;
	}

	/*
	 * =================== ManagedFunctionAdministrationMetaData ==================
	 */

	@Override
	public Logger getLogger() {
		return this.logger;
	}

	@Override
	public AdministrationMetaData<E, F, G> getAdministrationMetaData() {
		return this.administrationMetaData;
	}

}