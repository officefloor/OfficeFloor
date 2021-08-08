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
 * Context for the {@link ClassConstructorInterrogator}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassConstructorInterrogatorContext {

	/**
	 * Obtains the {@link Class} of object to be constructed for dependency
	 * injection.
	 * 
	 * @return {@link Class} of object to be constructed for dependency injection.
	 */
	Class<?> getObjectClass();

	/**
	 * <p>
	 * Provides optional error information about why a {@link Constructor} was not
	 * found.
	 * <p>
	 * This provides improved feedback to help resolve issue of why a
	 * {@link Constructor} was not selected in interrogation.
	 * 
	 * @param errorInformation Error information.
	 */
	void setErrorInformation(String errorInformation);

}
