/*-
 * #%L
 * Objectify Persistence
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

package net.officefloor.nosql.objectify;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.annotation.Entity;

/**
 * Locates {@link Objectify} {@link Entity} types for registering.
 * 
 * @author Daniel Sagenschneider
 */
public interface ObjectifyEntityLocator {

	/**
	 * Locates the {@link Objectify} {@link Entity} types.
	 * 
	 * @return {@link Objectify} {@link Entity} types.
	 * @throws Exception If fails to locate the {@link Objectify} {@link Entity}
	 *                   types.
	 */
	Class<?>[] locateEntities() throws Exception;

}
