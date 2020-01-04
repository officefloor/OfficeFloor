/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.compile.spi.office;

import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * {@link ManagedObject} contained within an {@link OfficeSubSection}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeSectionManagedObject extends DependentManagedObject,
		AdministerableManagedObject, GovernerableManagedObject {

	/**
	 * Obtains the name of this {@link OfficeSectionManagedObject}.
	 * 
	 * @return Name of this {@link OfficeSectionManagedObject}.
	 */
	String getOfficeSectionManagedObjectName();

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
