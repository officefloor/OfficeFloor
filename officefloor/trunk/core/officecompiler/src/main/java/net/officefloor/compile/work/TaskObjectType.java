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

package net.officefloor.compile.work;

import net.officefloor.frame.api.execute.Task;

/**
 * <code>Type definition</code> of a dependent {@link Object} required by the
 * {@link Task}.
 * 
 * @author Daniel Sagenschneider
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
	 * <p>
	 * Obtains the qualifier on the type.
	 * <p>
	 * This is to enable qualifying the type of dependency required.
	 * 
	 * @return Qualifier on the type. May be <code>null</code> if not qualifying
	 *         the type.
	 */
	String getTypeQualifier();

	/**
	 * Obtains the {@link Enum} key for the {@link TaskObjectType}.
	 * 
	 * @return {@link Enum} key for the {@link TaskObjectType}. May be
	 *         <code>null</code> if no {@link Enum} for objects.
	 */
	M getKey();

}