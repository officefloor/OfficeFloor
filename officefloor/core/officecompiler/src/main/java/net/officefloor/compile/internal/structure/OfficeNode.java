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
import net.officefloor.compile.office.OfficeType;
import net.officefloor.compile.spi.office.ExecutionExplorer;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSupplier;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.supplier.source.AvailableType;
import net.officefloor.compile.spi.supplier.source.InternalSupplier;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.UnknownFunctionException;
import net.officefloor.frame.api.profile.Profiler;

/**
 * {@link Office} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeNode extends LinkOfficeNode, ManagedObjectRegistry, OfficeTeamRegistry, OverrideProperties,
		OfficeArchitect, DeployedOffice {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Office";

	/**
	 * Initialises the {@link OfficeNode}.
	 * 
	 * @param officeSourceClassName {@link OfficeSource} class name.
	 * @param officeSource          Optional instantiated {@link OfficeSource}. May
	 *                              be <code>null</code>.
	 * @param officeLocation        Location of the {@link Office}.
	 */
	void initialise(String officeSourceClassName, OfficeSource officeSource, String officeLocation);

	/**
	 * Obtains the additional profiles.
	 * 
	 * @return Additional profiles.
	 */
	String[] getAdditionalProfiles();

	/**
	 * Adds a {@link OfficeManagedObjectSource} supplied from an
	 * {@link OfficeSupplier}.
	 * 
	 * @param managedObjectSourceName Name of the {@link OfficeManagedObjectSource}.
	 * @param suppliedManagedObject   {@link SuppliedManagedObjectSourceNode} to
	 *                                supply the {@link OfficeManagedObjectSource}.
	 * @return {@link OfficeManagedObjectSource}.
	 */
	OfficeManagedObjectSource addManagedObjectSource(String managedObjectSourceName,
			SuppliedManagedObjectSourceNode suppliedManagedObject);

	/**
	 * Sources this {@link Office} along with its top level {@link OfficeSection}
	 * instances into this {@link OfficeNode}.
	 * 
	 * @param managedObjectSourceVisitor {@link ManagedObjectSourceVisitor}.
	 * @param compileContext             {@link CompileContext}.
	 * @return <code>true</code> if successfully sourced. Otherwise
	 *         <code>false</code> with issue reported to the {@link CompilerIssues}.
	 */
	boolean sourceOfficeWithTopLevelSections(ManagedObjectSourceVisitor managedObjectSourceVisitor,
			CompileContext compileContext);

	/**
	 * Sources this {@link Office} and all descendant {@link Node} instances.
	 * 
	 * @param managedObjectSourceVisitor {@link ManagedObjectSourceVisitor}.
	 * @param compileContext             {@link CompileContext}.
	 * @return <code>true</code> if successfully sourced. Otherwise
	 *         <code>false</code> with issue reported to the {@link CompilerIssues}.
	 */
	boolean sourceOfficeTree(ManagedObjectSourceVisitor managedObjectSourceVisitor, AutoWirerVisitor autoWirerVisitor,
			CompileContext compileContext);

	/**
	 * Obtains the {@link OfficeFloorNode} containing this {@link OfficeNode}.
	 * 
	 * @return {@link OfficeFloorNode} containing this {@link OfficeNode}.
	 */
	OfficeFloorNode getOfficeFloorNode();

	/**
	 * Loads the {@link OfficeType}.
	 * 
	 * @param compileContext {@link CompileContext}.
	 * @return {@link OfficeType} or <code>null</code> if issue loading with issue
	 *         reported to the {@link CompilerIssues}.
	 */
	OfficeType loadOfficeType(CompileContext compileContext);

	/**
	 * Obtains the {@link AvailableType} instances.
	 * 
	 * @param compileContext {@link CompileContext}.
	 * @return {@link AvailableType} instances.
	 */
	AvailableType[] getAvailableTypes(CompileContext compileContext);

	/**
	 * Auto-wires the {@link OfficeObjectNode} instances that are unlinked.
	 * 
	 * @param autoWirer      {@link AutoWirer}.
	 * @param compileContext {@link CompileContext}.
	 */
	void autoWireObjects(AutoWirer<LinkObjectNode> autoWirer, CompileContext compileContext);

	/**
	 * Auto-wires the {@link OfficeTeamNode} instances that are unlinked.
	 * 
	 * @param autoWirer      {@link AutoWirer}.
	 * @param compileContext {@link CompileContext}.
	 */
	void autoWireTeams(AutoWirer<LinkTeamNode> autoWirer, CompileContext compileContext);

	/**
	 * Runs the {@link ExecutionExplorer} instances.
	 * 
	 * @param compileContext {@link CompileContext}.
	 * @return <code>true</code> if successfully explored execution.
	 */
	boolean runExecutionExplorers(CompileContext compileContext);

	/**
	 * Builds the {@link Office} for this {@link OfficeNode}.
	 * 
	 * @param builder        {@link OfficeFloorBuilder}.
	 * @param compileContext {@link CompileContext}.
	 * @param profiler       Optional {@link Profiler}. May be <code>null</code>.
	 * @return {@link OfficeBuilder} for the built {@link Office}.
	 */
	OfficeBindings buildOffice(OfficeFloorBuilder builder, CompileContext compileContext, Profiler profiler);

	/**
	 * Loads the {@link FunctionManager} instances to externally trigger this
	 * {@link OfficeNode}.
	 * 
	 * @param office {@link Office} for this {@link OfficeNode}.
	 * @throws UnknownFunctionException {@link UnknownFunctionException}.
	 */
	void loadExternalServicing(Office office) throws UnknownFunctionException;

	/**
	 * Obtains the {@link InternalSupplier} instances for the {@link Office}.
	 * 
	 * @return {@link InternalSupplier} instances for the {@link Office}.
	 */
	InternalSupplier[] getInternalSuppliers();

}
