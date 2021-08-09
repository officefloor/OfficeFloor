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

package net.officefloor.frame.impl.execute.office;

import net.officefloor.frame.api.manage.ObjectTimeoutException;
import net.officefloor.frame.api.manage.ObjectUser;
import net.officefloor.frame.internal.structure.MonitorClock;

/**
 * {@link ObjectUser} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ObjectUserImpl<O> implements ObjectUser<O> {

	/**
	 * Bound object name.
	 */
	private final String boundObjectName;

	/**
	 * {@link MonitorClock}.
	 */
	private final MonitorClock monitorClock;

	/**
	 * Loaded object.
	 */
	private O object = null;

	/**
	 * Possible failure.
	 */
	private Throwable failure = null;

	/**
	 * Instantiate.
	 * 
	 * @param boundObjectName Bound object name.
	 * @param monitorClock    {@link MonitorClock}.
	 */
	public ObjectUserImpl(String boundObjectName, MonitorClock monitorClock) {
		this.boundObjectName = boundObjectName;
		this.monitorClock = monitorClock;
	}

	/**
	 * Obtains the object. This method will block until the object is available, a
	 * failure or time out waiting.
	 * 
	 * @param timeoutInMilliseconds Time out in milliseconds to block waiting for
	 *                              object to become available.
	 * @return Object.
	 * @throws Throwable If fails to obtain the object.
	 */
	public synchronized O getObject(long timeoutInMilliseconds) throws Throwable {

		// Obtain the current type
		long currentTime = this.monitorClock.currentTimeMillis();

		// Loop until timeout
		while ((this.monitorClock.currentTimeMillis() - currentTime) <= timeoutInMilliseconds) {

			// Check if failure
			if (this.failure != null) {
				throw this.failure;
			}

			// Check if have object
			if (this.object != null) {
				return this.object;
			}

			// As here, still waiting (so wait some time)
			this.wait(10);
		}

		// As here timed out
		throw new ObjectTimeoutException(this.boundObjectName, timeoutInMilliseconds);
	}

	/*
	 * ===================== ObjectUser =========================
	 */

	@Override
	public synchronized void use(O object, Throwable failure) {

		// Load values
		this.object = object;
		this.failure = failure;

		// Notify available
		this.notify();
	}

}
