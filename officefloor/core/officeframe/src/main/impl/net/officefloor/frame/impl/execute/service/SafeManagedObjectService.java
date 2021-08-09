/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.frame.impl.execute.service;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.ProcessManager;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectService;
import net.officefloor.frame.api.managedobject.source.ManagedObjectServiceContext;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * Wraps the set up of the {@link ManagedObjectService} to block invocations
 * until available.
 * 
 * @author Daniel Sagenschneider
 */
public class SafeManagedObjectService<F extends Enum<F>> implements ManagedObjectServiceContext<F> {

	/**
	 * Safely invokes service on the {@link ManagedObjectServiceContext}.
	 */
	@FunctionalInterface
	public static interface SafeServicer<F extends Enum<F>, T extends Exception> {

		/**
		 * Invokes servicing on the {@link ManagedObjectServiceContext}.
		 * 
		 * @param serviceContext {@link ManagedObjectServiceContext}.
		 * @throws T If service failure.
		 */
		void service(ManagedObjectServiceContext<F> serviceContext) throws T;
	}

	/**
	 * Invokes the {@link ProcessState} from the
	 * {@link ManagedObjectServiceContext}.
	 */
	@FunctionalInterface
	public static interface ProcessInvoker<F extends Enum<F>> {

		/**
		 * Invokes the {@link ProcessState} from the
		 * {@link ManagedObjectServiceContext}.
		 * 
		 * @param serviceContext {@link ManagedObjectServiceContext}.
		 * @return {@link ProcessManager} from the invoked {@link ProcessState}.
		 */
		ProcessManager invokeProcess(ManagedObjectServiceContext<F> serviceContext);
	}

	/**
	 * {@link ManagedObjectServiceContext}.
	 */
	private ManagedObjectServiceContext<F> serviceContext;

	/**
	 * Flags that {@link OfficeFloor} has stopped servicing.
	 */
	private boolean isStopServicing = false;

	/**
	 * Initiate {@link ManagedObjectExecuteContext}.
	 * 
	 * @param executeContext {@link ManagedObjectExecuteContext}.
	 */
	public SafeManagedObjectService(ManagedObjectExecuteContext<F> executeContext) {
		this(executeContext, null);
	}

	/**
	 * Initiate {@link ManagedObjectExecuteContext}.
	 * 
	 * @param executeContext {@link ManagedObjectExecuteContext}.
	 * @param startup        Start up {@link SafeServicer}.
	 */
	public SafeManagedObjectService(ManagedObjectExecuteContext<F> executeContext,
			SafeServicer<F, ? extends Exception> startup) {

		// Each access to this
		SafeManagedObjectService<F> safe = this;

		// Register for servicing
		executeContext.addService(new ManagedObjectService<F>() {

			@Override
			public void startServicing(ManagedObjectServiceContext<F> serviceContext) throws Exception {
				synchronized (safe) {

					// Set up for servicing
					safe.serviceContext = serviceContext;
					try {

						// Undertake possible start up
						if (startup != null) {
							startup.service(serviceContext);
						}

					} finally {
						safe.notify(); // allow blocked poll to proceed
					}
				}
			}

			@Override
			public void stopServicing() {
				synchronized (safe) {
					safe.isStopServicing = true;
					safe.notify(); // allow immediate clean up
				}
			}
		});
	}

	/**
	 * Undertakes servicing.
	 * 
	 * @param servicer Logic for servicing.
	 * @return <code>true</code> if servicing invoked. <code>false</code> is stopped
	 *         servicing.
	 * @throws T                    {@link SafeServicer} possible failure.
	 * @throws InterruptedException If failed to waiting for servicing.
	 */
	public synchronized <T extends Exception> boolean service(SafeServicer<F, T> servicer)
			throws T, InterruptedException {
		for (;;) {

			// Determine if stop servicing
			if (this.isStopServicing) {
				return false; // no further servicing, so no further polling
			}

			// Poll if have service context
			if (this.serviceContext != null) {
				servicer.service(this.serviceContext);
				return true; // invoked
			}

			// Wait some time for service context
			this.wait(10);
		}
	}

	/**
	 * Enables generic {@link ProcessState} invocation.
	 * 
	 * @param invoker {@link ProcessInvoker}.
	 * @return {@link ProcessManager} of the invoked {@link ProcessState}.
	 */
	public ProcessManager invokeProcess(ProcessInvoker<F> invoker) {
		try {
			ProcessManager[] processManager = new ProcessManager[1];
			this.service((context) -> processManager[0] = invoker.invokeProcess(context));
			return processManager[0] != null ? processManager[0] : () -> {
				// Not invoked, so nothing to cancel
			};
		} catch (InterruptedException ex) {
			throw new IllegalStateException(
					"Interrupted in obtaining " + ManagedObjectServiceContext.class.getSimpleName());
		}

	}

	/*
	 * ==================== ManagedObjectServiceContext ====================
	 */

	@Override
	public ProcessManager invokeProcess(F key, Object parameter, ManagedObject managedObject, long delay,
			FlowCallback callback) throws IllegalArgumentException {
		return this.invokeProcess((context) -> context.invokeProcess(key, parameter, managedObject, delay, callback));
	}

	@Override
	public ProcessManager invokeProcess(int flowIndex, Object parameter, ManagedObject managedObject, long delay,
			FlowCallback callback) throws IllegalArgumentException {
		return this.invokeProcess(
				(context) -> context.invokeProcess(flowIndex, parameter, managedObject, delay, callback));
	}

}
