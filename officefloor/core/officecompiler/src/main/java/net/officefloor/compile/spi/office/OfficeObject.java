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
package net.officefloor.compile.spi.office;

import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorDependencyRequireNode;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * {@link Object} required by the {@link Office} that is to be provided by the
 * {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeObject extends OfficeDependencyObjectNode, OfficeFloorDependencyRequireNode,
		DependentManagedObject, AdministerableManagedObject, GovernerableManagedObject {

	/**
	 * Obtains the name that the {@link OfficeSource} refers to this
	 * {@link Object}.
	 * 
	 * @return Name that the {@link OfficeSource} refers to this {@link Object}.
	 */
	String getOfficeObjectName();

	/**
	 * Specifies the type qualifier.
	 * 
	 * @param qualifier
	 *            Type qualifier.
	 */
	void setTypeQualifier(String qualifier);

	/**
	 * <p>
	 * Adds an {@link OfficeAdministration} to be done before attempting load
	 * this {@link ManagedObject}.
	 * <p>
	 * The order that the {@link OfficeAdministration} instances are added is
	 * the order they will be done.
	 * 
	 * @param administration
	 *            {@link OfficeAdministration} to be done before attempting load
	 *            this {@link ManagedObject}.
	 */
	void addPreLoadAdministration(OfficeAdministration administration);

}