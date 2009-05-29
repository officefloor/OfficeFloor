/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.compile.spi.officefloor.source;

import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Enables the {@link OfficeFloorSource} to specify any required
 * {@link Property} instances necessary for loading the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface RequiredProperties {

	/**
	 * Adds a required {@link Property}.
	 * 
	 * @param name
	 *            Name of the {@link Property} which is also used as its label.
	 */
	void addRequiredProperty(String name);

	/**
	 * Adds a required {@link Property}.
	 * 
	 * @param name
	 *            Name of the {@link Property}.
	 * @param label
	 *            Descriptive label for the {@link Property}.
	 */
	void addRequiredProperty(String name, String label);

}