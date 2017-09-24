/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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

import net.officefloor.compile.section.TypeQualification;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.team.Team;

/**
 * Augmented {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AugmentedManagedObject {

	/**
	 * Obtains the name of the {@link ManagedObject}.
	 * 
	 * @return Name of the {@link ManagedObject}.
	 */
	String getAugmentedManagedObjectName();

	/**
	 * <p>
	 * Adds an {@link TypeQualification} for this
	 * {@link AugmentedManagedObject}.
	 * <p>
	 * This enables distinguishing {@link AugmentedManagedObject} instances to
	 * enable, for example, dynamic {@link Team} assignment.
	 * 
	 * @param qualifier
	 *            Qualifier. May be <code>null</code> if no qualification.
	 * @param type
	 *            Type (typically the fully qualified type).
	 */
	void addTypeQualification(String qualifier, String type);

}