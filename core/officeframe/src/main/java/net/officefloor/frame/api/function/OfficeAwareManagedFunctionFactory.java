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

package net.officefloor.frame.api.function;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * <p>
 * {@link Office} aware {@link ManagedFunctionFactory}.
 * <p>
 * This allows the {@link ManagedFunctionFactory} to:
 * <ol>
 * <li>obtain the dynamic meta-data of its containing {@link Office}</li>
 * <li>ability to spawn {@link ProcessState} instances</li>
 * </ol>
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeAwareManagedFunctionFactory<O extends Enum<O>, F extends Enum<F>>
		extends ManagedFunctionFactory<O, F> {

	/**
	 * Provides the {@link ManagedFunctionFactory} its containing
	 * {@link Office}.
	 * 
	 * @param office
	 *            {@link Office} containing this {@link ManagedFunctionFactory}.
	 * @throws Exception
	 *             If fails to use the {@link Office}.
	 */
	void setOffice(Office office) throws Exception;

}
