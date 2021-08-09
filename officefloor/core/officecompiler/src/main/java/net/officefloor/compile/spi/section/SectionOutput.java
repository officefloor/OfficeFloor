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
import net.officefloor.compile.section.SectionOutputType;

/**
 * Output of a {@link SectionNode}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionOutput extends SectionFlowSinkNode {

	/**
	 * Obtains the name of this {@link SectionOutput}.
	 * 
	 * @return Name of this {@link SectionOutput}.
	 */
	String getSectionOutputName();

	/**
	 * <p>
	 * Adds the annotation for this {@link SectionOutput}.
	 * <p>
	 * This is exposed as is on the {@link SectionOutputType} interface for this
	 * {@link SectionOutput}.
	 * 
	 * @param annotation
	 *            Annotation.
	 */
	void addAnnotation(Object annotation);

}
