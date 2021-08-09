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
 * <code>Type definition</code> of an input for a {@link SectionType}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionInputType extends AnnotatedType {

	/**
	 * Obtains the name of this {@link SectionInputType}.
	 * 
	 * @return Name of this {@link SectionInputType}.
	 */
	String getSectionInputName();

	/**
	 * <p>
	 * Obtains the fully qualified {@link Class} name of the parameter type for this
	 * {@link SectionInputType}.
	 * <p>
	 * The name is returned rather than the actual {@link Class} to enable the
	 * {@link SectionType} to be obtained should the {@link Class} not be available
	 * to the {@link ClassLoader}.
	 * 
	 * @return Fully qualified {@link Class} name of the parameter type.
	 */
	String getParameterType();

}
