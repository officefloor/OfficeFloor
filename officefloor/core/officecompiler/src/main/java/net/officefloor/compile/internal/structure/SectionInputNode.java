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

import java.util.Map;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.section.OfficeSectionInputType;
import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.spi.office.ExecutionExplorer;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SubSectionInput;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.UnknownFunctionException;

/**
 * {@link SectionInput} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionInputNode
		extends LinkFlowNode, SectionInput, SubSectionInput, OfficeSectionInput, DeployedOfficeInput {

	/**
	 * {@link Node} type.
	 */
	String TYPE = "Section Input";

	/**
	 * Initialises this {@link SectionInputType}.
	 * 
	 * @param parameterType
	 *            Parameter type.
	 */
	void initialise(String parameterType);

	/**
	 * Loads the {@link FunctionManager} instances to externally trigger this
	 * {@link SectionInputNode}.
	 * 
	 * @param office
	 *            {@link Office} containing this {@link SectionInputNode}.
	 * @throws UnknownFunctionException
	 *             {@link UnknownFunctionException}.
	 */
	void loadExternalServicing(Office office) throws UnknownFunctionException;

	/**
	 * Loads the {@link SectionInputType}.
	 * 
	 * @param compileContext
	 *            {@link CompileContext}.
	 * @return {@link SectionInputType} or <code>null</code> with issues
	 *         reported to the {@link CompilerIssues}.
	 */
	SectionInputType loadSectionInputType(CompileContext compileContext);

	/**
	 * Loads the {@link OfficeSectionInputType}.
	 * 
	 * @param compileContext
	 *            {@link CompileContext}.
	 * @return {@link OfficeSectionInputType} or <code>null</code> with issues
	 *         reported to the {@link CompilerIssues}.
	 */
	OfficeSectionInputType loadOfficeSectionInputType(CompileContext compileContext);

	/**
	 * Runs the {@link ExecutionExplorer} instances.
	 * 
	 * @param managedFunctions
	 *            {@link ManagedFunctionNode} instances by their qualified name.
	 * @param compileContext
	 *            {@link CompileContext}.
	 * @return <code>true</code> if successfully explored execution.
	 */
	boolean runExecutionExplorers(Map<String, ManagedFunctionNode> managedFunctions, CompileContext compileContext);

}
