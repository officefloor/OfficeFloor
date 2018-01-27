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
package net.officefloor.frame.api.build;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * <p>
 * Provides the mappings of the dependencies of a {@link ManagedObject} to the
 * {@link ManagedObject} providing necessary functionality.
 * <p>
 * This works within the scope of where the {@link ManagedObject} is being
 * added.
 * 
 * @author Daniel Sagenschneider
 */
public interface DependencyMappingBuilder {

	/**
	 * Specifies the {@link ManagedObject} for the dependency key.
	 * 
	 * @param <O>
	 *            Dependency key type.
	 * @param key
	 *            Key of the dependency.
	 * @param scopeManagedObjectName
	 *            Name of the {@link ManagedObject} within the scope that this
	 *            {@link DependencyMappingBuilder} was created.
	 */
	<O extends Enum<O>> void mapDependency(O key, String scopeManagedObjectName);

	/**
	 * Specifies the {@link ManagedObject} for the index identifying the
	 * dependency.
	 * 
	 * @param index
	 *            Index identifying the dependency.
	 * @param scopeManagedObjectName
	 *            Name of the {@link ManagedObject} within the scope that this
	 *            {@link DependencyMappingBuilder} was created.
	 */
	void mapDependency(int index, String scopeManagedObjectName);

	/**
	 * Specifies the {@link Governance} for the {@link ManagedObject}.
	 * 
	 * @param governanceName
	 *            Name of the {@link Governance} within the {@link Office}.
	 */
	void mapGovernance(String governanceName);

	/**
	 * Adds {@link Administration} to be undertaken before this
	 * {@link ManagedObject} is loaded.
	 * 
	 * @param administrationName
	 *            Name of the {@link Administration}.
	 * @param extension
	 *            Extension type for {@link Administration}.
	 * @param administrationFactory
	 *            {@link AdministrationFactory}.
	 * @return {@link AdministrationBuilder} to build the
	 *         {@link Administration}.
	 */
	<E, f extends Enum<f>, G extends Enum<G>> AdministrationBuilder<f, G> preLoadAdminister(String administrationName,
			Class<E> extension, AdministrationFactory<E, f, G> administrationFactory);

}