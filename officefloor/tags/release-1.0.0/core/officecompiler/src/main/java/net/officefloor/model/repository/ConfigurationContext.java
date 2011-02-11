/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

package net.officefloor.model.repository;

import java.io.InputStream;

/**
 * Context of the {@link ConfigurationItem} instances.
 *
 * @author Daniel Sagenschneider
 */
public interface ConfigurationContext {

	/**
	 * <p>
	 * Obtains the location for this {@link ConfigurationContext}.
	 * <p>
	 * This value is used in the equality of {@link ConfigurationContext}
	 * instances.
	 *
	 * @return Location for this {@link ConfigurationContext}.
	 */
	String getLocation();

	/**
	 * Obtains the class path for the context.
	 *
	 * @return Class path for the context.
	 */
	String[] getClasspath();

	/**
	 * Obtains the {@link ConfigurationItem} at the relative location.
	 *
	 * @param relativeLocation
	 *            Relative location of the {@link ConfigurationItem} to obtain.
	 * @return {@link ConfigurationItem}.
	 * @throws Exception
	 *             If can not obtain a {@link ConfigurationItem} at the relative
	 *             location.
	 */
	ConfigurationItem getConfigurationItem(String relativeLocation)
			throws Exception;

	/**
	 * Creates a new {@link ConfigurationItem} at the relative location.
	 *
	 * @param relativeLocation
	 *            Relative location of the {@link ConfigurationItem} to create.
	 * @param configuration
	 *            Configuration for the {@link ConfigurationItem}.
	 * @return The created {@link ConfigurationItem}.
	 * @throws Exception
	 *             If fails to create the {@link ConfigurationItem}.
	 */
	ConfigurationItem createConfigurationItem(String relativeLocation,
			InputStream configuration) throws Exception;

}