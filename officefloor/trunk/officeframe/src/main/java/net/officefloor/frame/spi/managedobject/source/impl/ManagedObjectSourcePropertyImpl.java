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
package net.officefloor.frame.spi.managedobject.source.impl;

import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceProperty;


/**
 * Implementation of the
 * {@link net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceProperty}.
 * 
 * @author Daniel
 */
public class ManagedObjectSourcePropertyImpl implements
		ManagedObjectSourceProperty {

	/**
	 * Name of property.
	 */
	protected final String name;

	/**
	 * Label of property.
	 */
	protected final String label;

	/**
	 * Initiate with name and label of property.
	 * 
	 * @param name
	 *            Name of property.
	 * @param label
	 *            Label of property.
	 */
	public ManagedObjectSourcePropertyImpl(String name, String label) {
		// Store state
		this.name = name;
		this.label = label;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.core.spi.objectsource.ManagedObjectSourceProperty#getName()
	 */
	public String getName() {
		return this.name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.core.spi.objectsource.ManagedObjectSourceProperty#getLabel()
	 */
	public String getLabel() {
		return this.label;
	}

}
