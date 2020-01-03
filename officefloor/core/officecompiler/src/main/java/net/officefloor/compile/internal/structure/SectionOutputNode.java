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