/*
 * Created on Jan 10, 2006
 */
package net.officefloor.frame.api.managedobject.source;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * <p>
 * Source to obtain a particular type of {@link ManagedObject}.
 * <p>
 * Implemented by the {@link ManagedObject} provider.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectSource<O extends Enum<O>, F extends Enum<F>> {

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
	 * Initialises the {@link ManagedObjectSource}.
	 * 
	 * @param context
	 *            {@link ManagedObjectSourceContext} to use in initialising.
	 * @return Meta-data to describe this.
	 * @throws Exception
	 *             Should the {@link ManagedObjectSource} fail to configure
	 *             itself from the input properties.
	 */
	ManagedObjectSourceMetaData<O, F> init(ManagedObjectSourceContext<F> context) throws Exception;

	/**
	 * <p>
	 * Called once after {@link #init(ManagedObjectSourceContext)} to indicate
	 * this {@link ManagedObjectSource} should start execution.
	 * <p>
	 * On invocation of this method, {@link ProcessState} instances may be
	 * invoked via the {@link ManagedObjectExecuteContext}.
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

	/**
	 * <p>
	 * Called to notify that the {@link OfficeFloor} is being closed.
	 * <p>
	 * On return from this method, no further {@link ProcessState} instances may
	 * be invoked.
	 */
	void stop();

}