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
package net.officefloor.plugin.managedobject.clazz;

import java.lang.reflect.Field;

import net.officefloor.frame.spi.managedobject.ObjectRegistry;

/**
 * Meta-data for a {@link Dependency}.
 * 
 * @author Daniel
 */
public class DependencyMetaData {

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
	 * @param index
	 *            Index of the dependency within the {@link ObjectRegistry}.
	 * @param field
	 *            {@link Field} to receive the injected dependency.
	 */
	public DependencyMetaData(int index, Field field) {
		this.index = index;
		this.field = field;
	}

}