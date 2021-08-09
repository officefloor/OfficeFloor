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

/**
 * Creates the {@link ManagedFunction} to be executed.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionFactory<O extends Enum<O>, F extends Enum<F>> {

	/**
	 * Creates the {@link ManagedFunction}.
	 * 
	 * @return {@link ManagedFunction}.
	 * @throws Throwable
	 *             If fails to create the {@link ManagedFunction}.
	 */
	ManagedFunction<O, F> createManagedFunction() throws Throwable;

}
