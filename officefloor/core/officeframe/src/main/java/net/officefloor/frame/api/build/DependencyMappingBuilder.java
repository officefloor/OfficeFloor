/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.frame.api.build;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.Flow;

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
	 * Specifies the {@link ManagedObject} for the index identifying the dependency.
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
	 * @param <E>
	 *            Extension type.
	 * @param <f>
	 *            {@link Flow} key type.
	 * @param <G>
	 *            {@link Governance} key type.
	 * @param administrationName
	 *            Name of the {@link Administration}.
	 * @param extension
	 *            Extension type for {@link Administration}.
	 * @param administrationFactory
	 *            {@link AdministrationFactory}.
	 * @return {@link AdministrationBuilder} to build the {@link Administration}.
	 */
	<E, f extends Enum<f>, G extends Enum<G>> AdministrationBuilder<f, G> preLoadAdminister(String administrationName,
			Class<E> extension, AdministrationFactory<E, f, G> administrationFactory);

}
