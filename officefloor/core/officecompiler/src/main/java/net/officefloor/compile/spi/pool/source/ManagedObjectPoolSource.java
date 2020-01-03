package net.officefloor.compile.spi.pool.source;

import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;

/**
 * Sources a {@link ManagedObjectPool}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectPoolSource {

	/**
	 * <p>
	 * Obtains the specification for this.
	 * <p>
	 * This will be called before any other methods, therefore this method must
	 * be able to return the specification immediately after a default
	 * constructor instantiation.
	 * 
	 * @return Specification of this.
	 */
	ManagedObjectPoolSourceSpecification getSpecification();

	/**
	 * Initialises and configures the {@link ManagedObjectPoolSource}.
	 * 
	 * @param context
	 *            {@link ManagedObjectPoolSourceContext}.
	 * @return {@link ManagedObjectPoolSourceMetaData} for the
	 *         {@link ManagedObjectPool}.
	 * @throws Exception
	 *             If fails to configure the {@link ManagedObjectPoolSource}.
	 */
	ManagedObjectPoolSourceMetaData init(ManagedObjectPoolSourceContext context) throws Exception;

}