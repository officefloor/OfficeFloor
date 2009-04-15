/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.compile.properties;

import java.util.Properties;

/**
 * Listing of {@link Property} instances.
 * 
 * @author Daniel
 */
public interface PropertyList extends Iterable<Property> {

	/**
	 * Appends a {@link Property} to this {@link PropertyList}.
	 * 
	 * @param name
	 *            Name of the {@link Property}.
	 * @param label
	 *            Label of the {@link Property}. Should this be blank it will be
	 *            defaulted to the name.
	 * @return {@link Property} added.
	 */
	Property addProperty(String name, String label);

	/**
	 * Appends a {@link Property} to this {@link PropertyList}.
	 * 
	 * @param name
	 *            Name of the {@link Property} which is also used as the label.
	 * @return {@link Property} added.
	 */
	Property addProperty(String name);

	/**
	 * Obtains the names of the {@link Property} instances in the order they
	 * were added.
	 * 
	 * @return Names of the {@link Property} instances.
	 */
	String[] getPropertyNames();

	/**
	 * Obtains the first {@link Property} by the input name.
	 * 
	 * @param name
	 *            Name of the {@link Property} to return.
	 * @return First {@link Property} by the input name, or <code>null</code> if
	 *         no {@link Property} by the name.
	 */
	Property getProperty(String name);

	/**
	 * Obtains the {@link Properties} populated with the {@link Property}
	 * values.
	 * 
	 * @return Populated {@link Properties}.
	 */
	Properties getProperties();

}