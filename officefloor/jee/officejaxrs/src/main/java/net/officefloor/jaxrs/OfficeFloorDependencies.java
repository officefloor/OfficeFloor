/*-
 * #%L
 * JAX-RS
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

package net.officefloor.jaxrs;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.glassfish.jersey.internal.inject.Injectee;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Dependencies available from {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorDependencies {

	/**
	 * Dependencies.
	 */
	private final Map<AnnotatedElement, Object> dependencies = new HashMap<>();

	/**
	 * Registers a {@link Field} dependency.
	 * 
	 * @param field      {@link Field}.
	 * @param dependency Dependency.
	 */
	public void registerFieldDependency(Field field, Object dependency) {
		this.dependencies.put(field, dependency);
	}

	/**
	 * Obtains the dependency for the {@link Injectee}.
	 * 
	 * @param annotatedElement {@link AnnotatedElement}. Will gracefully handle
	 *                         <code>null</code>.
	 * @return Dependency or <code>null</code> if not matched.
	 */
	public Object getDependency(AnnotatedElement annotatedElement) {
		return annotatedElement == null ? null : this.dependencies.get(annotatedElement);
	}

}
