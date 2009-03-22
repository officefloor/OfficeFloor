/*
 * Created on Jan 10, 2006
 */
package net.officefloor.frame.spi.managedobject.source;

import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * <p>
 * Source to obtain a particular type of {@link ManagedObject}.
 * <p>
 * Implemented by the {@link ManagedObject} provider.
 * 
 * @author Daniel
 */
public interface ManagedObjectSource<D extends Enum<D>, F extends Enum<F>> {

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
	ManagedObjectSourceSpecification getSpecification();

	/**
	 * Called only once after the {@link ManagedObjectSource} is instantiated.
	 * 
	 * @param context
	 *            {@link ManagedObjectSourceContext} to use in initialising.
	 * @throws Exception
	 *             Should the {@link ManagedObjectSource} fail to configure
	 *             itself from the input properties.
	 */
	void init(ManagedObjectSourceContext<F> context) throws Exception;

	/**
	 * <p>
	 * Obtains the meta-data to describe this.
	 * <p>
	 * This is called after the {@link #init(ManagedObjectSourceContext)} method
	 * and therefore may use the configuration.
	 * <p>
	 * This should always return non-null. If there is a problem due to
	 * incorrect configuration, the {@link #init(ManagedObjectSourceContext)}
	 * should indicate this via an exception.
	 * 
	 * @return Meta-data to describe this.
	 */
	ManagedObjectSourceMetaData<D, F> getMetaData();

	/**
	 * Called once after {@link #init(ManagedObjectSourceContext)} to indicate
	 * this {@link ManagedObjectSource} should start execution.
	 * 
	 * @param context
	 *            {@link ManagedObjectExecuteContext} to use in starting this
	 *            {@link ManagedObjectSource}.
	 * @throws Exception
	 *             Should the {@link ManagedObjectSource} fail to start
	 *             execution.
	 */
	void start(ManagedObjectExecuteContext<F> context) throws Exception;

	/**
	 * Sources a {@link ManagedObject} from this {@link ManagedObjectSource}.
	 * 
	 * @param user
	 *            {@link ManagedObjectUser} interested in using the
	 *            {@link ManagedObject}.
	 */
	void sourceManagedObject(ManagedObjectUser user);

}