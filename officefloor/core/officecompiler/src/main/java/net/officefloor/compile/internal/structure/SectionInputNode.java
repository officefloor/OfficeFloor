/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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