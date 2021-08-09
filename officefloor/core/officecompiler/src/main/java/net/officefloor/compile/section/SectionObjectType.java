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

package net.officefloor.compile.section;

import net.officefloor.compile.type.AnnotatedType;

/**
 * <code>Type definition</code> of an {@link Object} dependency required by the
 * {@link SectionType}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionObjectType extends AnnotatedType {

	/**
	 * Obtains the name of this {@link SectionObjectType}.
	 * 
	 * @return Name of this {@link SectionObjectType}.
	 */
	String getSectionObjectName();

	/**
	 * <p>
	 * Obtains the fully qualified {@link Class} name of the {@link Object} type for
	 * this {@link SectionObjectType}.
	 * <p>
	 * The name is returned rather than the actual {@link Class} to enable the
	 * {@link SectionType} to be obtained should the {@link Class} not be available
	 * to the {@link ClassLoader}.
	 * 
	 * @return Fully qualified {@link Class} name of the {@link Object} type.
	 */
	String getObjectType();

	/**
	 * <p>
	 * Obtains the qualifier on the type.
	 * <p>
	 * This is to enable qualifying the type of dependency required.
	 * 
	 * @return Qualifier on the type. May be <code>null</code> if not qualifying the
	 *         type.
	 */
	String getTypeQualifier();

}
