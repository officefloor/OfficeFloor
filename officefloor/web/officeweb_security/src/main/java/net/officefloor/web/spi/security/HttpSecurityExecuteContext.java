package net.officefloor.web.spi.security;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.ProcessManager;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectStartupProcess;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * <p>
 * Context that the {@link HttpSecurity} is to execute within.
 * <p>
 * This is similar to the {@link ManagedObjectExecuteContext}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecurityExecuteContext<F extends Enum<F>> {

	/**
	 * Registers a start up {@link Flow}.
	 * 
	 * @param key       Key identifying the {@link Flow} to instigate.
	 * @param parameter Parameter to first {@link ManagedFunction} of the
	 *                  {@link Flow}.
	 * @param callback  {@link FlowCallback} on completion of the {@link Flow}.
	 * @return {@link ManagedObjectStartupProcess}.
	 * @throws IllegalArgumentException If
	 *                                  <ul>
	 *                                  <li>unknown {@link Flow} key</li>
	 *                                  <li>parameter is incorrect type</li>
	 *                                  </ul>
	 */
	ManagedObjectStartupProcess registerStartupProcess(F key, Object parameter, FlowCallback callback)
			throws IllegalArgumentException;

	/**
	 * Instigates a {@link Flow}.
	 * 
	 * @param key       Key identifying the {@link Flow} to instigate.
	 * @param parameter Parameter to first {@link ManagedFunction} of the
	 *                  {@link Flow}.
	 * @param delay     Delay in milliseconds before the {@link Flow} is invoked. A
	 *                  <code>0</code> or negative value invokes the {@link Flow}
	 *                  immediately.
	 * @param callback  {@link FlowCallback} on completion of the {@link Flow}.
	 * @return {@link ProcessManager} for the {@link ProcessState}.
	 * @throws IllegalArgumentException If
	 *                                  <ul>
	 *                                  <li>unknown {@link Flow} key</li>
	 *                                  <li>parameter is incorrect type</li>
	 *                                  </ul>
	 */
	ProcessManager invokeProcess(F key, Object parameter, long delay, FlowCallback callback)
			throws IllegalArgumentException;

}