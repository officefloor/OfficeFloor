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
package net.officefloor.compile.spi.pool.source.impl;

import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSourceProperty;

/**
 * {@link ManagedObjectPoolSourceProperty} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectPoolSourcePropertyImpl implements ManagedObjectPoolSourceProperty {

	/**
	 * Label.
	 */
	private final String label;

	/**
	 * Name.
	 */
	private final String name;

	/**
	 * Initialise.
	 * 
	 * @param name
	 *            Name.
	 * @param label
	 *            Label.
	 */
	public ManagedObjectPoolSourcePropertyImpl(String name, String label) {
		this.label = label;
		this.name = name;
	}

	/*
	 * ==================== ManagedObjectPoolSourceProperty ====================
	 */

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getLabel() {
		return this.label;
	}

}