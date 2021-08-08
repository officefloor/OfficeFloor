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

package net.officefloor.web.value.load;

/**
 * Factory to create the {@link StatelessValueLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public interface StatelessValueLoaderFactory {

	/**
	 * Obtains the property name for the {@link StatelessValueLoader}.
	 * 
	 * @return Property name for the {@link StatelessValueLoader}.
	 */
	String getPropertyName();

	/**
	 * Creates the {@link StatelessValueLoader}.
	 * 
	 * @param clazz
	 *            {@link StatelessValueLoader} will be specific to the {@link Class}.
	 * @return {@link StatelessValueLoader}.
	 * @throws Exception
	 *             If fails to create the {@link StatelessValueLoader}.
	 */
	StatelessValueLoader createValueLoader(Class<?> clazz) throws Exception;

}
