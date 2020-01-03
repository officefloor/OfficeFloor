package net.officefloor.frame.internal.structure;

import java.util.concurrent.Executor;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Meta-data of the {@link Administration}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministrationMetaData<E, F extends Enum<F>, G extends Enum<G>> extends ManagedFunctionLogicMetaData {

	/**
	 * Obtains the name of the {@link Administration}.
	 * 
	 * @return Name of the {@link Administration}.
	 */
	String getAdministrationName();

	/**
	 * Obtains the {@link AdministrationFactory}.
	 * 
	 * @return {@link AdministrationFactory}.
	 */
	AdministrationFactory<E, F, G> getAdministrationFactory();

	/**
	 * Obtains the extension interface to administer the {@link ManagedObject}
	 * instances.
	 * 
	 * @return Extension interface to administer the {@link ManagedObject}
	 *         instances.
	 */
	Class<E> getExtensionInterface();

	/**
	 * Obtains the {@link ManagedObjectExtensionExtractorMetaData} over the
	 * {@link ManagedObject} instances to be administered by this
	 * {@link Administration}.
	 * 
	 * @return {@link ManagedObjectExtensionExtractorMetaData} over the
	 *         {@link ManagedObject} instances to be administered by this
	 *         {@link Administration}.
	 */
	ManagedObjectExtensionExtractorMetaData<E>[] getManagedObjectExtensionExtractorMetaData();

	/**
	 * Translates the {@link Administration} {@link Governance} index to the
	 * {@link ThreadState} {@link Governance} index.
	 * 
	 * @param governanceIndex {@link Administration} {@link Governance} index.
	 * @return {@link ThreadState} {@link Governance} index.
	 */
	int translateGovernanceIndexToThreadIndex(int governanceIndex);

	/**
	 * Obtains the {@link Executor} for {@link AdministrationContext}.
	 * 
	 * @return {@link Executor} for {@link AdministrationContext}.
	 */
	Executor getExecutor();

}