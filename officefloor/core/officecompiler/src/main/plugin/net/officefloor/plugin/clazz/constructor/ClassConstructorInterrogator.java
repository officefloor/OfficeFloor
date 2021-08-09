/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.plugin.clazz.constructor;

import java.lang.reflect.Constructor;

/**
 * Interrogates the {@link Class} for a {@link Constructor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassConstructorInterrogator {

	/**
	 * Interrogates for the {@link Constructor}.
	 * 
	 * @param context {@link ClassConstructorInterrogatorContext}.
	 * @return {@link Constructor}. Should be <code>null</code> to allow another
	 *         {@link ClassConstructorInterrogator} to find the {@link Constructor}.
	 * @throws Exception If fails to interrogate.
	 */
	Constructor<?> interrogate(ClassConstructorInterrogatorContext context) throws Exception;

}
