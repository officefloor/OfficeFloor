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
package net.officefloor.frame.spi.managedobject;

/**
 * Name aware {@link ManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
public interface NameAwareManagedObject extends ManagedObject {

	/**
	 * Provides the {@link ManagedObject} its bound name.
	 *
	 * @param boundManagedObjectName
	 *            Bound name for the {@link ManagedObject}.
	 */
	void setBoundManagedObjectName(String boundManagedObjectName);

}