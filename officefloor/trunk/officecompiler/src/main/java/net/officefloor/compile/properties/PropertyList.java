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

import java.util.List;

/**
 * Listing of {@link Property} instances.
 * 
 * @author Daniel
 */
public interface PropertyList {

	/**
	 * Obtains the internal list of {@link Property} instances. Changing the
	 * return list changes the state of this {@link PropertyList}.
	 * 
	 * @return Internal {@link Property} instances.
	 */
	List<Property> getPropertyList();

	/**
	 * Appends a {@link Property} to this {@link PropertyList}.
	 * 
	 * @param name
	 *            Name of the {@link Property}.
	 * @param label
	 *            Label of the {@link Property}. Should this be blank it will be
	 *            defaulted to the name.
	 */
	void addProperty(String name, String label);

	/**
	 * Appends a {@link Property} to this {@link PropertyList}.
	 * 
	 * @param name
	 *            Name of the {@link Property} which is also used as the label.
	 */
	void addProperty(String name);

}