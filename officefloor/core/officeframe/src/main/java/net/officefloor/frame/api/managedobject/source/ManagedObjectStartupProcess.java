package net.officefloor.frame.api.managedobject.source;

import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * Start up {@link ProcessState} registered via a
 * {@link ManagedObjectExecuteContext}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectStartupProcess {

	/**
	 * <p>
	 * Flags for the {@link ProcessState} to be invoked concurrently.
	 * <p>
	 * The start up {@link ProcessState} is executed with on same {@link Thread}
	 * building the {@link OfficeFloor}. This allows for the start up
	 * {@link ProcessState} instances to complete before the {@link OfficeFloor} is
	 * opened (unless using another {@link Team}).
	 * <p>
	 * By flagging concurrent, it allows another {@link Thread} to concurrently
	 * invoke the start up {@link ProcessState}. This allows concurrent start up,
	 * albeit requiring concurrency handling due to using multiple {@link Thread}
	 * instances.
	 * <p>
	 * Furthermore, as order of execution of the start up {@link ProcessState}
	 * instances is based on the {@link OfficeFrame}, it allows any dependency
	 * ordering to be resolved. This is because they both can be executed
	 * concurrently and co-ordinate themselves together.
	 * 
	 * @param isConcurrent Flags whether to undertake the start up
	 *                     {@link ProcessState} concurrently.
	 */
	void setConcurrent(boolean isConcurrent);

}