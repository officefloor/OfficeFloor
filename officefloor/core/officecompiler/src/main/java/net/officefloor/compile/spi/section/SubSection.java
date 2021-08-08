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

package net.officefloor.compile.spi.section;

import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.properties.PropertyConfigurable;

/**
 * {@link SubSection} of an {@link SectionNode}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SubSection extends PropertyConfigurable {

	/**
	 * Obtains the name of this {@link SubSection}.
	 * 
	 * @return Name of this {@link SubSection}.
	 */
	String getSubSectionName();

	/**
	 * Obtains the {@link SubSectionInput}.
	 * 
	 * @param inputName
	 *            Name of the {@link SubSectionInput} to obtain.
	 * @return {@link SubSectionInput}.
	 */
	SubSectionInput getSubSectionInput(String inputName);

	/**
	 * Obtains the {@link SubSectionOutput}.
	 * 
	 * @param outputName
	 *            Name of the {@link SubSectionOutput} to obtain.
	 * @return {@link SubSectionOutput}.
	 */
	SubSectionOutput getSubSectionOutput(String outputName);

	/**
	 * Obtains the {@link SubSectionObject}.
	 * 
	 * @param objectName
	 *            Name of the {@link SubSectionObject} to obtain.
	 * @return {@link SubSectionObject}.
	 */
	SubSectionObject getSubSectionObject(String objectName);

}
