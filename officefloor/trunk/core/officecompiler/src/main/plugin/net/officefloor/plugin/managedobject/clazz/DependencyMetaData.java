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

package net.officefloor.plugin.managedobject.clazz;

import java.lang.reflect.Field;

import net.officefloor.frame.spi.managedobject.ObjectRegistry;

/**
 * Meta-data for a {@link Dependency}.
 * 
 * @author Daniel Sagenschneider
 */
public class DependencyMetaData {

	/**
	 * Name of the dependency.
	 */
	public final String name;

	/**
	 * Index of the dependency within the {@link ObjectRegistry}.
	 */
	public final int index;

	/**
	 * {@link Field} to receive the injected dependency.
	 */
	public final Field field;

	/**
	 * Initiate.
	 * 
	 * @param name
	 *            Name of the dependency.
	 * @param index
	 *            Index of the dependency within the {@link ObjectRegistry}.
	 * @param field
	 *            {@link Field} to receive the injected dependency.
	 */
	public DependencyMetaData(String name, int index, Field field) {
		this.name = name;
		this.index = index;
		this.field = field;
	}

}