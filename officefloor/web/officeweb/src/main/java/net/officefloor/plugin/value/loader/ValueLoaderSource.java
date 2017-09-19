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
package net.officefloor.plugin.value.loader;

import java.util.Map;

/**
 * Sources the {@link StatelessValueLoader} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface ValueLoaderSource {

	/**
	 * Initialise.
	 * 
	 * @param type
	 *            Type to interrogate for properties to be loaded.
	 * @param isCaseSensitive
	 *            Flag indicating if case sensitive matching.
	 * @param aliasMappings
	 *            Alias Mappings.
	 * @param objectInstantiator
	 *            {@link ObjectInstantiator}.
	 * @throws Exception
	 *             If fails to initialise.
	 */
	void init(Class<?> type, boolean isCaseSensitive,
			Map<String, String> aliasMappings,
			ObjectInstantiator objectInstantiator) throws Exception;

	/**
	 * <p>
	 * Sources the {@link ValueLoaderFactory}.
	 * <p>
	 * The returned {@link ValueLoaderFactory} is specific to the input
	 * {@link Class}. The {@link ValueLoaderFactory} will not work with children
	 * of the {@link Class}. A new {@link ValueLoaderFactory} must be created
	 * for each child type.
	 * 
	 * @param <T>
	 *            {@link Class} type.
	 * @param clazz
	 *            {@link Class} for a dedicated {@link ValueLoaderFactory}. It
	 *            is expected that all property methods on the <code>type</code>
	 *            are on this <code>clazz</code>.
	 * @return {@link ValueLoaderFactory}.
	 * @throws Exception
	 *             If fails to source the {@link ValueLoaderFactory}.
	 */
	<T> ValueLoaderFactory<T> sourceValueLoaderFactory(Class<T> clazz)
			throws Exception;

}