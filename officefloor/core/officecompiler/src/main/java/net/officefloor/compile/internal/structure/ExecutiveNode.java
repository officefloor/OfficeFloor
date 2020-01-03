package net.officefloor.compile.internal.structure;

import net.officefloor.compile.executive.ExecutiveType;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.spi.officefloor.OfficeFloorExecutive;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.source.ExecutiveSource;

/**
 * {@link OfficeFloorExecutive} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface ExecutiveNode extends Node, OfficeFloorExecutive {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Executive";

	/**
	 * Initialises the {@link ExecutiveNode}.
	 * 
	 * @param executiveSourceClassName Class name of the {@link ExecutiveSource}.
	 * @param executiveSource          Optional instantiated
	 *                                 {@link ExecutiveSource}. May be
	 *                                 <code>null</code>.
	 */
	void initialise(String executiveSourceClassName, ExecutiveSource executiveSource);

	/**
	 * Loads the {@link ExecutiveType} for the {@link ExecutiveSource}.
	 * 
	 * @return {@link ExecutiveType} or <code>null</code> with issues reported to
	 *         the {@link CompilerIssues}.
	 */
	ExecutiveType loadExecutiveType();

	/**
	 * Builds the {@link Executive} for this {@link ExecutiveNode}.
	 * 
	 * @param builder        {@link OfficeFloorBuilder}.
	 * @param compileContext {@link CompileContext}.
	 */
	void buildExecutive(OfficeFloorBuilder builder, CompileContext compileContext);

}