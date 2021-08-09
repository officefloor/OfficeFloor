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
import net.officefloor.compile.section.SectionInputType;

/**
 * Input to an {@link SectionNode}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionInput extends SectionFlowSourceNode {

	/**
	 * Obtains the name of this {@link SectionInput}.
	 * 
	 * @return Name of this {@link SectionInput}.
	 */
	String getSectionInputName();

	/**
	 * <p>
	 * Adds the annotation for this {@link SectionInput}.
	 * <p>
	 * This is exposed as is on the {@link SectionInputType} interface for this
	 * {@link SectionInput}.
	 * 
	 * @param annotation
	 *            Annotation.
	 */
	void addAnnotation(Object annotation);

}
