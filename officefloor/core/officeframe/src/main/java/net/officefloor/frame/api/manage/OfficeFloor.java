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

package net.officefloor.frame.api.manage;

import net.officefloor.frame.api.function.ManagedFunction;

/**
 * {@link OfficeFloor} where {@link ManagedFunction} instances are executed
 * within {@link Office} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloor extends AutoCloseable {

	/**
	 * Opens the OfficeFloor and starts necessary {@link ManagedFunction} instances.
	 * 
	 * @throws Exception If fails to open the OfficeFloor.
	 */
	void openOfficeFloor() throws Exception;

	/**
	 * Closes the OfficeFloor. This stops all {@link ManagedFunction} instances
	 * executing within the {@link Office} instances and releases all resources.
	 * 
	 * @throws Exception If fails to close the {@link OfficeFloor}.
	 */
	default void closeOfficeFloor() throws Exception {
		this.close();
	}

	/**
	 * <p>
	 * Obtains the names of the {@link Office} instances within this
	 * {@link OfficeFloor}.
	 * <p>
	 * This allows to dynamically manage this {@link OfficeFloor}.
	 * 
	 * @return Names of the {@link Office} instances within this
	 *         {@link OfficeFloor}.
	 */
	String[] getOfficeNames();

	/**
	 * Obtains the {@link Office} for the input office name.
	 * 
	 * @param officeName Name of the {@link Office}.
	 * @return Specified {@link Office}.
	 * @throws UnknownOfficeException If no {@link Office} by the name within this
	 *                                {@link OfficeFloor}.
	 */
	Office getOffice(String officeName) throws UnknownOfficeException;

}
