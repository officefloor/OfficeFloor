/*-
 * #%L
 * Web Plug-in
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

package net.officefloor.web.session;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.AsynchronousManagedObject;

/**
 * Administration interface for the {@link HttpSession}.
 *
 * @author Daniel Sagenschneider
 */
public interface HttpSessionAdministration {

	/**
	 * Triggers invalidating the {@link HttpSession}.
	 *
	 * @param isRequireNewSession
	 *            <code>true</code> to have a new {@link HttpSession} created.
	 * @throws Throwable
	 *             If immediate failure in invalidating the {@link HttpSession}.
	 */
	void invalidate(boolean isRequireNewSession) throws Throwable;

	/**
	 * Triggers storing the {@link HttpSession}.
	 *
	 * @throws Throwable
	 *             If immediate failure in storing the {@link HttpSession}.
	 */
	void store() throws Throwable;

	/**
	 * <p>
	 * Indicates if the invalidate or store operation are complete.
	 * <p>
	 * As is an {@link AsynchronousManagedObject}, the next time a new
	 * {@link ManagedFunction} is run the operation should be complete. This method enables
	 * determining if completed immediately and there were no failures of the
	 * operation.
	 *
	 * @return <code>true</code> if the invalidate or store operation is
	 *         complete.
	 * @throws Throwable
	 *             Possible failure in invalidating or storing the
	 *             {@link HttpSession}.
	 */
	boolean isOperationComplete() throws Throwable;

}
