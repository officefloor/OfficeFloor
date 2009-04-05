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
package net.officefloor.compile.work;

import net.officefloor.frame.api.execute.Task;

/**
 * <code>Type definition</code> of a dependent {@link Object} required by the
 * {@link Task}.
 * 
 * @author Daniel
 */
public interface TaskObjectType<M extends Enum<M>> {

	/**
	 * Obtains the name for the {@link TaskObjectType}.
	 * 
	 * @return Name for the {@link TaskObjectType}.
	 */
	String getObjectName();

	/**
	 * <p>
	 * Obtains the index for the {@link TaskObjectType}.
	 * <p>
	 * Should there be an {@link Enum} then will be the {@link Enum#ordinal()}
	 * value. Otherwise will be the index that this was added.
	 * 
	 * @return Index for the {@link TaskObjectType}.
	 */
	int getIndex();

	/**
	 * Obtains the required type of the dependent {@link Object}.
	 * 
	 * @return Required type of the dependent {@link Object}.
	 */
	Class<?> getObjectType();

	/**
	 * Obtains the {@link Enum} key for the {@link TaskObjectType}.
	 * 
	 * @return {@link Enum} key for the {@link TaskObjectType}. May be
	 *         <code>null</code> if no {@link Enum} for objects.
	 */
	M getKey();

}