/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.building.decorate;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * Locates the {@link OfficeFloorDecorator} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorDecoratorServiceLoader {

	/**
	 * <p>
	 * Locates the {@link OfficeFloorDecorator} instances.
	 * <p>
	 * To configure an {@link OfficeFloorDecorator} to be located follow the
	 * configuration of a {@link ServiceLoader} for an
	 * {@link OfficeFloorDecorator}.
	 * 
	 * @param classLoader
	 *            {@link ClassLoader} to utilise. May be <code>null</code>.
	 * @return {@link OfficeFloorDecorator} instances.
	 */
	public static OfficeFloorDecorator[] loadOfficeFloorDecorators(
			ClassLoader classLoader) {

		// Ensure have the class loader
		if (classLoader == null) {
			classLoader = Thread.currentThread().getContextClassLoader();
		}

		// Load the decorators
		ServiceLoader<OfficeFloorDecorator> decorators = ServiceLoader.load(
				OfficeFloorDecorator.class, classLoader);

		// Load unique set of decorators (i.e. 1 decorator of each type only)
		List<OfficeFloorDecorator> uniqueDecorators = new LinkedList<OfficeFloorDecorator>();
		Set<Class<?>> registeredDecoratorTypes = new HashSet<Class<?>>();
		for (OfficeFloorDecorator decorator : decorators) {
			if (!registeredDecoratorTypes.contains(decorator.getClass())) {
				uniqueDecorators.add(decorator);
				registeredDecoratorTypes.add(decorator.getClass());
			}
		}

		// Return the unique listing of decorators
		return uniqueDecorators
				.toArray(new OfficeFloorDecorator[uniqueDecorators.size()]);
	}

	/**
	 * All access via static methods.
	 */
	private OfficeFloorDecoratorServiceLoader() {
	}

}