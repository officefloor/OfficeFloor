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
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.section.OfficeFunctionType;
import net.officefloor.compile.section.OfficeSubSectionType;
import net.officefloor.compile.spi.office.AugmentedFunctionObject;
import net.officefloor.compile.spi.office.ExecutionManagedFunction;
import net.officefloor.compile.spi.office.OfficeSectionFunction;
import net.officefloor.compile.spi.office.ResponsibleTeam;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.function.ManagedFunction;

/**
 * {@link SectionFunction} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionNode extends LinkFlowNode, SectionFunction, OfficeSectionFunction {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Managed Function";

	/**
	 * Obtains the fully qualified name of this {@link ManagedFunction}.
	 * 
	 * @return Fully qualified name of this {@link ManagedFunction}.
	 */
	String getQualifiedFunctionName();

	/**
	 * Initialises this {@link ManagedFunctionNode}.
	 * 
	 * @param managedFunctionTypeName
	 *            {@link ManagedFunctionType} name.
	 * @param functionNamespace
	 *            {@link FunctionNamespaceNode} for the
	 *            {@link ManagedFunctionNode}.
	 */
	void initialise(String managedFunctionTypeName, FunctionNamespaceNode functionNamespace);

	/**
	 * Sources the {@link ManagedFunction}.
	 * 
	 * @param managedFunctionVisitor
	 *            {@link ManagedFunctionVisitor}.
	 * @param compileContext
	 *            {@link CompileContext}.
	 * @return <code>true</code> if successfully sourced the
	 *         {@link ManagedFunction}. <code>false</code> if failed to source
	 *         with issues reported to the {@link CompilerIssues}.
	 */
	boolean souceManagedFunction(ManagedFunctionVisitor managedFunctionVisitor, CompileContext compileContext);

	/**
	 * Obtains the {@link AugmentedFunctionObject}.
	 * 
	 * @param objectName
	 *            Name of the {@link AugmentedFunctionObject}.
	 * @return {@link AugmentedFunctionObject}.
	 */
	AugmentedFunctionObject getAugmentedFunctionObject(String objectName);

	/**
	 * Auto wires the {@link ResponsibleTeam} for this {@link ManagedFunction}.
	 * 
	 * @param autoWirer
	 *            {@link AutoWirer}.
	 * @param compileContext
	 *            {@link CompileContext}.
	 */
	void autoWireManagedFunctionResponsibility(AutoWirer<LinkTeamNode> autoWirer, CompileContext compileContext);

	/**
	 * Loads the {@link OfficeFunctionType}.
	 * 
	 * @param parentSubSectionType
	 *            Containing {@link OfficeSubSectionType} to this
	 *            {@link OfficeSectionFunction}.
	 * @param compileContext
	 *            {@link CompileContext}.
	 * @return {@link OfficeFunctionType} or <code>null</code> with issues
	 *         reported to the {@link CompilerIssues}.
	 */
	OfficeFunctionType loadOfficeFunctionType(OfficeSubSectionType parentSubSectionType, CompileContext compileContext);

	/**
	 * Obtains the {@link FunctionNamespaceNode} containing this
	 * {@link ManagedFunctionNode}.
	 * 
	 * @return {@link FunctionNamespaceNode} containing this
	 *         {@link ManagedFunctionNode}.
	 */
	FunctionNamespaceNode getFunctionNamespaceNode();

	/**
	 * Loads the {@link ManagedFunctionType} for this
	 * {@link ManagedFunctionNode}.
	 * 
	 * @param compileContext
	 *            {@link CompileContext}.
	 * @return {@link ManagedFunctionType} for this {@link ManagedFunctionNode}.
	 *         May be <code>null</code> if can not determine
	 *         {@link ManagedFunctionType}.
	 */
	ManagedFunctionType<?, ?> loadManagedFunctionType(CompileContext compileContext);

	/**
	 * Creates an {@link ExecutionManagedFunction} for this
	 * {@link ManagedFunctionNode}.
	 * 
	 * @param compileContext
	 *            {@link CompileContext}.
	 * @return {@link ExecutionManagedFunction} for this
	 *         {@link ManagedFunctionNode}.
	 */
	ExecutionManagedFunction createExecutionManagedFunction(CompileContext compileContext);

	/**
	 * Builds the {@link ManagedFunction} for this {@link ManagedFunctionNode}.
	 * 
	 * @param officeBuilder
	 *            {@link OfficeBuilder}.
	 * @param compileContext
	 *            {@link CompileContext}.
	 */
	void buildManagedFunction(OfficeBuilder officeBuilder, CompileContext compileContext);

}
