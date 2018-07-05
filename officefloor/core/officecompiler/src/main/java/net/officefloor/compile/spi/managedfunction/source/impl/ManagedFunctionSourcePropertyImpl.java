/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.spi.managedfunction.source.impl;

import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceProperty;

/**
 * {@link ManagedFunctionSourceProperty} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionSourcePropertyImpl implements ManagedFunctionSourceProperty {

	/**
	 * Name.
	 */
	private final String name;

	/**
	 * Label.
	 */
	private final String label;

	/**
	 * Initiate.
	 * 
	 * @param name
	 *            Name.
	 * @param label
	 *            Label. Defaults to <code>name</code> if <code>null</code>.
	 */
	public ManagedFunctionSourcePropertyImpl(String name, String label) {
		this.name = name;
		this.label = ((label == null) || (label.trim().length() == 0)) ? name
				: label;
	}

	/*
	 * ====================== WorkSourceProperty ==============================
	 */

	@Override
	public String getLabel() {
		return this.label;
	}

	@Override
	public String getName() {
		return this.name;
	}

}