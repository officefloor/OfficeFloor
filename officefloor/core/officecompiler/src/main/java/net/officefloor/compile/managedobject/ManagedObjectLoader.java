/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.managedobject;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.officefloor.OfficeFloorManagedObjectSourceType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceProperty;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceSpecification;

/**
 * Loads the {@link ManagedObjectType} from the {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectLoader {

	/**
	 * Loads and returns the {@link PropertyList} from the
	 * {@link ManagedObjectSourceSpecification} for the {@link ManagedObjectSource}.
	 * 
	 * @param <D>                      Dependency key type.
	 * @param <F>                      Flow key type.
	 * @param <MS>                     {@link ManagedObjectSource} type.
	 * @param managedObjectSourceClass {@link ManagedObjectSource} class.
	 * @return {@link PropertyList} of the {@link ManagedObjectSourceProperty}
	 *         instances of the {@link ManagedObjectSourceSpecification} or
	 *         <code>null</code> if issue, which is reported to the
	 *         {@link CompilerIssues}.
	 */
	<D extends Enum<D>, F extends Enum<F>, MS extends ManagedObjectSource<D, F>> PropertyList loadSpecification(
			Class<MS> managedObjectSourceClass);

	/**
	 * Loads and returns the {@link PropertyList} from the
	 * {@link ManagedObjectSourceSpecification} for the {@link ManagedObjectSource}.
	 * 
	 * @param <D>                 Dependency key type.
	 * @param <F>                 Flow key type.
	 * @param managedObjectSource {@link ManagedObjectSource} instance.
	 * @return {@link PropertyList} of the {@link ManagedObjectSourceProperty}
	 *         instances of the {@link ManagedObjectSourceSpecification} or
	 *         <code>null</code> if issue, which is reported to the
	 *         {@link CompilerIssues}.
	 */
	<D extends Enum<D>, F extends Enum<F>> PropertyList loadSpecification(
			ManagedObjectSource<D, F> managedObjectSource);

	/**
	 * Loads and returns the {@link ManagedObjectType} for the
	 * {@link ManagedObjectSource}.
	 * 
	 * @param <D>                      Dependency key type.
	 * @param <F>                      Flow key type.
	 * @param <MS>                     {@link ManagedObjectSource} type.
	 * @param managedObjectSourceClass Class of the {@link ManagedObjectSource}.
	 * @param propertyList             {@link PropertyList} containing the
	 *                                 properties to source the
	 *                                 {@link ManagedObjectType}.
	 * @return {@link ManagedObjectType} or <code>null</code> if issues, which are
	 *         reported to the {@link CompilerIssues}.
	 */
	<D extends Enum<D>, F extends Enum<F>, MS extends ManagedObjectSource<D, F>> ManagedObjectType<D> loadManagedObjectType(
			Class<MS> managedObjectSourceClass, PropertyList propertyList);

	/**
	 * Loads and returns the {@link ManagedObjectType} for the
	 * {@link ManagedObjectSource}.
	 * 
	 * @param <D>                 Dependency key type.
	 * @param <F>                 Flow key type.
	 * @param managedObjectSource {@link ManagedObjectSource} instance to use.
	 * @param propertyList        {@link PropertyList} containing the properties to
	 *                            source the {@link ManagedObjectType}.
	 * @return {@link ManagedObjectType} or <code>null</code> if issues, which are
	 *         reported to the {@link CompilerIssues}.
	 */
	<D extends Enum<D>, F extends Enum<F>> ManagedObjectType<D> loadManagedObjectType(
			ManagedObjectSource<D, F> managedObjectSource, PropertyList propertyList);

	/**
	 * Loads and returns the {@link OfficeFloorManagedObjectSourceType} for the
	 * {@link ManagedObjectSource}.
	 * 
	 * @param <D>                      Dependency key type.
	 * @param <F>                      Flow key type.
	 * @param <MS>                     {@link ManagedObjectSource} type.
	 * @param managedObjectSourceName  Name of the {@link ManagedObjectSource}.
	 * @param managedObjectSourceClass Class of the {@link ManagedObjectSource}.
	 * @param propertyList             {@link PropertyList} containing the
	 *                                 properties to source the
	 *                                 {@link OfficeFloorManagedObjectSourceType}.
	 * @return {@link OfficeFloorManagedObjectSourceType} or <code>null</code> if
	 *         issues, which are reported to the {@link CompilerIssues}.
	 */
	<D extends Enum<D>, F extends Enum<F>, MS extends ManagedObjectSource<D, F>> OfficeFloorManagedObjectSourceType loadOfficeFloorManagedObjectSourceType(
			String managedObjectSourceName, Class<MS> managedObjectSourceClass, PropertyList propertyList);

	/**
	 * Loads and returns the {@link OfficeFloorManagedObjectSourceType} for the
	 * {@link ManagedObjectSource}.
	 * 
	 * @param <D>                     Dependency key type.
	 * @param <F>                     Flow key type.
	 * @param <MS>                    {@link ManagedObjectSource} type.
	 * @param managedObjectSourceName Name of the {@link ManagedObjectSource}.
	 * @param managedObjectSource     {@link ManagedObjectSource} instances to use.
	 * @param propertyList            {@link PropertyList} containing the properties
	 *                                to source the
	 *                                {@link OfficeFloorManagedObjectSourceType}.
	 * @return {@link OfficeFloorManagedObjectSourceType} or <code>null</code> if
	 *         issues, which are reported to the {@link CompilerIssues}.
	 */
	<D extends Enum<D>, F extends Enum<F>, MS extends ManagedObjectSource<D, F>> OfficeFloorManagedObjectSourceType loadOfficeFloorManagedObjectSourceType(
			String managedObjectSourceName, MS managedObjectSource, PropertyList propertyList);

}
