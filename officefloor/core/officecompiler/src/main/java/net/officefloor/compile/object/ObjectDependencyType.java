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

package net.officefloor.compile.object;

import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.spi.office.OfficeSectionFunction;

/**
 * <code>Type definition</code> of a dependent object of an {@link OfficeSectionFunction}.
 *
 * @author Daniel Sagenschneider
 */
public interface ObjectDependencyType {

	/**
	 * <p>
	 * Obtains the name of this object dependency.
	 * <p>
	 * This would correspond to either the {@link ManagedFunctionObjectType} or the
	 * {@link ManagedObjectDependencyType} name.
	 * 
	 * @return Name of this object dependency.
	 */
	String getObjectDependencyName();

	/**
	 * Obtains the type required of this object dependency.
	 * 
	 * @return Type required of this object dependency.
	 */
	String getObjectDependencyType();

	/**
	 * Obtains the type qualifier required of this object dependency.
	 * 
	 * @return Type qualifier required of this object dependency. May be
	 *         <code>null</code> if no qualifier.
	 */
	String getObjectDependencyTypeQualifier();

	/**
	 * Indicates if the object dependency is a parameter.
	 * 
	 * @return <code>true</code> if object dependency is a parameter.
	 */
	boolean isParameter();

	/**
	 * <p>
	 * Obtains the object that fulfills the dependency.
	 * <p>
	 * Should the {@link ObjectDependencyType} represent a parameter then no
	 * {@link DependentObjectType} will be provided.
	 * 
	 * @return {@link DependentObjectType} or <code>null</code> if parameter or
	 *         unable to obtain {@link DependentObjectType}.
	 */
	DependentObjectType getDependentObjectType();

}
