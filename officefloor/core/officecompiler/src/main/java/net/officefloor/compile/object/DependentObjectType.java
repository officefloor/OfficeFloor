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
