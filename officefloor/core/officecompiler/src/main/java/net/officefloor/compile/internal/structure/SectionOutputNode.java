/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.compile.internal.structure;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.section.OfficeSectionOutputType;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.SubSectionOutput;

/**
 * {@link SectionOutput} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionOutputNode extends LinkFlowNode, SectionOutput,
		SubSectionOutput, OfficeSectionOutput {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Section Output";

	/**
	 * Initialises this {@link SectionOutputType}.
	 * 
	 * @param argumentType
	 *            Argument type.
	 * @param isEscalationOnly
	 *            Flag indicating if escalation only.
	 */
	void initialise(String argumentType, boolean isEscalationOnly);

	/**
	 * Obtains {@link SectionNode} containing this {@link SectionOutputNode}.
	 * 
	 * @return {@link SectionNode} containing this {@link SectionOutputNode}.
	 */
	SectionNode getSectionNode();

	/**
	 * Loads the {@link SectionOutputType}.
	 * 
	 * @param compileContext
	 *            {@link CompileContext}.
	 * @return {@link SectionOutputType} or <code>null</code> with issues
	 *         reported to the {@link CompilerIssues}.
	 */
	SectionOutputType loadSectionOutputType(CompileContext compileContext);

	/**
	 * Loads the {@link OfficeSectionOutputType}.
	 * 
	 * @param compileContext
	 *            {@link CompileContext}.
	 * @return {@link OfficeSectionOutputType} or <code>null</code> with issues
	 *         reported to the {@link CompilerIssues}.
	 */
	OfficeSectionOutputType loadOfficeSectionOutputType(CompileContext compileContext);

}
