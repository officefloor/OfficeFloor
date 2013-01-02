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
package net.officefloor.plugin.woof;

import java.io.File;

import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.frame.spi.source.ResourceSource;

/**
 * WoOF item that is configurable with context details.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofContextConfigurable extends PropertyConfigurable {

	/**
	 * <p>
	 * Provides location of the <code>webapp</code> directory.
	 * <p>
	 * Note that this may not be called if the <code>webapp</code> directory is
	 * not available.
	 * 
	 * @param webappDirectory
	 *            Location of the <code>webapp</code> directory.
	 */
	void setWebAppDirectory(File webappDirectory);

	/**
	 * Adds a {@link ResourceSource}.
	 * 
	 * @param resourceSource
	 *            {@link ResourceSource}.
	 */
	void addResources(ResourceSource resourceSource);

}