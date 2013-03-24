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
package net.officefloor.compile.impl.util;

import net.officefloor.compile.administrator.AdministratorType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.office.OfficeType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.work.WorkType;

/**
 * Propagates a failure loading a type.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadTypeError extends Error {

	/**
	 * Type being attempted to be loaded.
	 * 
	 * @see #getType()
	 */
	private final Class<?> type;

	/**
	 * Name of the source class being used to load the type. May also be an
	 * alias.
	 */
	private final String sourceClassName;

	/**
	 * Initiate.
	 * 
	 * @param type
	 *            Type being attempted to be loaded.
	 * @param sourceClassName
	 *            Name of the source class being used to load the type. May also
	 *            be an alias.
	 */
	public LoadTypeError(Class<?> type, String sourceClassName) {
		this.type = type;
		this.sourceClassName = sourceClassName;
	}

	/**
	 * Obtains the type being attempted to be loaded. Value should be one of
	 * following:
	 * <ol>
	 * <li>{@link WorkType}</li>
	 * <li>{@link ManagedObjectType}</li>
	 * <li>{@link SectionType}</li>
	 * <li>{@link AdministratorType}</li>
	 * <li>{@link OfficeType}</li>
	 * </ol>
	 * 
	 * @return Type being attempted to be loaded.
	 */
	public Class<?> getType() {
		return this.type;
	}

	/**
	 * Obtains the name of the source class being used to load the type. May
	 * also be an alias.
	 * 
	 * @return Name of the source class being used to load the type. May also be
	 *         an alias.
	 */
	public String getSourceClassName() {
		return this.sourceClassName;
	}

}