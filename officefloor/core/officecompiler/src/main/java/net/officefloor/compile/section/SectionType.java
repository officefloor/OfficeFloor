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

import net.officefloor.frame.api.manage.Office;

/**
 * <code>Type definition</code> of a section of the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionType {

	/**
	 * Obtains the {@link SectionInputType} definitions for the inputs into the
	 * {@link SectionType}.
	 * 
	 * @return {@link SectionInputType} definitions for the inputs into the
	 *         {@link SectionType}.
	 */
	SectionInputType[] getSectionInputTypes();

	/**
	 * Obtains the {@link SectionOutputType} definitions for the outputs from
	 * the {@link SectionType}.
	 * 
	 * @return {@link SectionOutputType} definitions for the outputs from the
	 *         {@link SectionType}.
	 */
	SectionOutputType[] getSectionOutputTypes();

	/**
	 * Obtains the {@link SectionObjectType} definitions for the {@link Object}
	 * dependencies required by the {@link SectionType}.
	 * 
	 * @return {@link SectionObjectType} definitions for the {@link Object}
	 *         dependencies required by the {@link SectionType}.
	 */
	SectionObjectType[] getSectionObjectTypes();

}
