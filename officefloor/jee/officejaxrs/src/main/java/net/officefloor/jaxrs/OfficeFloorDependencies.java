/*-
 * #%L
 * JAX-RS
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
