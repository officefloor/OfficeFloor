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

package net.officefloor.compile.internal.structure;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.section.OfficeSectionObjectType;
import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SubSectionObject;

/**
 * {@link SectionObject} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionObjectNode extends LinkObjectNode, SubSectionObject,
		SectionObject, OfficeSectionObject, DependentObjectNode {

	/**
	 * {@link Node} type.
	 */
	String TYPE = "Section Object";

	/**
	 * Initialises this {@link SectionObjectType}.
	 * 
	 * @param objectType
	 *            Object type.
	 */
	void initialise(String objectType);

	/**
	 * Obtains {@link SectionNode} containing this {@link SectionObjectNode}.
	 * 
	 * @return {@link SectionNode} containing this {@link SectionObjectNode}.
	 */
	SectionNode getSectionNode();

	/**
	 * Loads the {@link SectionObjectType}.
	 * 
	 * @param compileContext
	 *            {@link CompileContext}.
	 * @return {@link SectionObjectType} or <code>null</code> with issue
	 *         reported to the {@link CompilerIssues}.
	 */
	SectionObjectType loadSectionObjectType(CompileContext compileContext);

	/**
	 * Loads the {@link OfficeSectionObjectType}.
	 * 
	 * @param compileContext
	 *            {@link CompileContext}.
	 * @return {@link OfficeSectionObjectType} or <code>null</code> with issue
	 *         reported to the {@link CompilerIssues}.
	 */
	OfficeSectionObjectType loadOfficeSectionObjectType(CompileContext compileContext);

}
