/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.frame.spi.managedobject;

/**
 * Root interface of a Managed Object.
 *
 * @author Daniel Sagenschneider
 */
public interface ManagedObject {

	/**
	 * Obtains the object being managed.
	 *
	 * @return Object being managed.
	 * @throws Throwable
	 *             Indicating failed to obtain the object for use.
	 */
	Object getObject() throws Throwable;

}