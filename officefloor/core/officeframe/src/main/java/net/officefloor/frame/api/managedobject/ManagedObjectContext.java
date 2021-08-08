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

package net.officefloor.frame.api.managedobject;

import java.util.logging.Logger;

/**
 * Context for the {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectContext {

	/**
	 * <p>
	 * Obtains the name this {@link ManagedObject} is bound under.
	 * <p>
	 * This is useful to have a unique name identifying the {@link ManagedObject}.
	 * 
	 * @return Name this {@link ManagedObject} is bound under.
	 */
	String getBoundName();

	/**
	 * Obtains the {@link Logger} for the {@link ManagedObject}.
	 * 
	 * @return {@link Logger} for the {@link ManagedObject}.
	 */
	Logger getLogger();

	/**
	 * Undertakes a {@link ProcessSafeOperation}.
	 * 
	 * @param <R>       Return type from operation
	 * @param <T>       Possible {@link Throwable} type from operation.
	 * @param operation {@link ProcessSafeOperation}.
	 * @return Return value.
	 * @throws T Possible {@link Throwable}.
	 */
	<R, T extends Throwable> R run(ProcessSafeOperation<R, T> operation) throws T;

}
