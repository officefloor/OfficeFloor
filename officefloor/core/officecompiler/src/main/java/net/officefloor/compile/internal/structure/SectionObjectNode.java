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