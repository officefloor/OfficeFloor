/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.object;

import net.officefloor.compile.section.TypeQualification;

/**
 * <code>Type definition</code> for a dependent object.
 *
 * @author Daniel Sagenschneider
 */
public interface DependentObjectType {

	/**
	 * Obtains the name of this dependent object.
	 * 
	 * @return Name of this dependent object.
	 */
	String getDependentObjectName();

	/**
	 * <p>
	 * Obtains the {@link TypeQualification} instances for this
	 * {@link DependentObjectType}.
	 * <p>
	 * Should no {@link TypeQualification} instances be manually assigned, the
	 * {@link TypeQualification} should be derived from the object type (i.e.
	 * type without qualifier).
	 * 
	 * @return {@link TypeQualification} instances for this dependent object.
	 */
	TypeQualification[] getTypeQualifications();

	/**
	 * <p>
	 * Obtains the {@link ObjectDependencyType} instances for this dependent
	 * object.
	 * <p>
	 * This allows determining transitive dependencies.
	 * 
	 * @return {@link ObjectDependencyType} instances for this dependent object.
	 */
	ObjectDependencyType[] getObjectDependencies();

}