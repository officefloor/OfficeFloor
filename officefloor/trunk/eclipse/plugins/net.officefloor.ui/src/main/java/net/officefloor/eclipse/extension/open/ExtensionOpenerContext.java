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
package net.officefloor.eclipse.extension.open;

import net.officefloor.compile.properties.PropertyList;

/**
 * Context for the {@link ExtensionOpenerContext}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ExtensionOpenerContext {

	/**
	 * Obtains the {@link PropertyList} for the source.
	 * 
	 * @return {@link PropertyList} for the source.
	 */
	PropertyList getPropertyList();

	/**
	 * Opens the resource on the class path.
	 * 
	 * @param resourcePath
	 *            Path of the resource on the class path.
	 */
	void openClasspathResource(String resourcePath);

}