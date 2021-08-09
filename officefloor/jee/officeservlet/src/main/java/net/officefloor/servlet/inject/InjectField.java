/*-
 * #%L
 * Servlet
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

package net.officefloor.servlet.inject;

import java.lang.reflect.Field;

/**
 * Field to have dependency injected.
 * 
 * @author Daniel Sagenschneider
 */
public class InjectField {

	/**
	 * {@link Field} to load the dependency.
	 */
	public final Field field;

	/**
	 * Dependency to inject.
	 */
	public final Object dependency;

	/**
	 * Instantiate.
	 * 
	 * @param field      {@link Field} to load the dependency.
	 * @param dependency Dependency to inject.
	 */
	InjectField(Field field, Object dependency) {
		this.field = field;
		this.dependency = dependency;
	}
}
