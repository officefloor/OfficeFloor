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
import net.officefloor.compile.officefloor.OfficeFloorType;
import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorSupplier;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.spi.supplier.source.AvailableType;
import net.officefloor.compile.spi.supplier.source.InternalSupplier;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.build.OfficeFloorListener;
import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.UnknownFunctionException;
import net.officefloor.frame.api.manage.UnknownOfficeException;

/**
 * {@link OfficeFloor} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorNode
		extends Node, PropertyConfigurable, OverrideProperties, ManagedObjectRegistry, OfficeFloorDeployer {

	/**
	 * Default name of the {@link OfficeFloorNode}.
	 */
	static String OFFICE_FLOOR_NAME = "OfficeFloor";

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "OfficeFloor";

	/**
	 * Initialises the {@link OfficeFloorNode}.
	 */
	void initialise();

	/**
	 * Adds a {@link OfficeFloorManagedObjectSource} supplied from an
	 * {@link OfficeFloorSupplier}.
	 * 
	 * @param managedObjectSourceName Name of the
	 *                                {@link OfficeFloorManagedObjectSource}.
	 * @param suppliedManagedObject   {@link SuppliedManagedObjectSourceNode} to
	 *                                supply the
	 *                                {@link OfficeFloorManagedObjectSource}.
	 * @return {@link OfficeFloorManagedObjectSource}.
	 */
	OfficeFloorManagedObjectSource addManagedObjectSource(String managedObjectSourceName,
			SuppliedManagedObjectSourceNode suppliedManagedObject);

	/**
	 * <p>
	 * Sources the {@link OfficeFloor} into this {@link OfficeFloorNode}.
	 * <p>
	 * This will only source the top level {@link OfficeSection}.
	 * 
	 * @param compileContext {@link CompileContext}.
	 * @return <code>true</code> if successfully sourced. Otherwise
	 *         <code>false</code> with issue reported to the {@link CompilerIssues}.
	 */
	boolean sourceOfficeFloor(CompileContext compileContext);

	/**
	 * Sources this {@link OfficeFloorNode} and all its descendant {@link Node}
	 * instances recursively.
	 * 
	 * @param autoWirerVisitor {@link AutoWirerVisitor}.
	 * @param compileContext   {@link CompileContext}.
	 * @return <code>true</code> if successfully sourced. Otherwise
	 *         <code>false</code> with issue reported to the {@link CompilerIssues}.
	 */
	boolean sourceOfficeFloorTree(AutoWirerVisitor autoWirerVisitor, CompileContext compileContext);

	/**
	 * Obtains the {@link AvailableType} instances.
	 * 
	 * @param compileContext {@link CompileContext}.
	 * @return {@link AvailableType} instances.
	 */
	AvailableType[] getAvailableTypes(CompileContext compileContext);

	/**
	 * Loads the {@link AutoWire} targets for the {@link OfficeFloorManagedObject}
	 * instances.
	 * 
	 * @param autoWirer      {@link AutoWirer} to be loaded with the
	 *                       {@link OfficeFloorManagedObject} targets.
	 * @param compileContext {@link CompileContext}.
	 * @return {@link AutoWirer} with context for the {@link OfficeFloor}.
	 */
	AutoWirer<LinkObjectNode> loadAutoWireObjectTargets(AutoWirer<LinkObjectNode> autoWirer,
			CompileContext compileContext);

	/**
	 * Loads the {@link AutoWire} targets for the {@link ManagedObjectSourceNode}
	 * instances.
	 * 
	 * @param autoWirer      {@link AutoWirer} to be loaded with the
	 *                       {@link OfficeFloorManagedObjectSource} targets.
	 * @param compileContext {@link CompileContext}.
	 * @return {@link AutoWirer} with context for the {@link OfficeFloor}.
	 */
	AutoWirer<ManagedObjectSourceNode> loadAutoWireManagedObjectSourceTargets(
			AutoWirer<ManagedObjectSourceNode> autoWirer, CompileContext compileContext);

	/**
	 * Loads the {@link AutoWire} extension targets for the
	 * {@link OfficeFloorManagedObject}.
	 * 
	 * @param autoWirer      {@link AutoWirer} to be loaded with the
	 *                       {@link OfficeFloorManagedObject} extension targets.
	 * @param compileContext {@link CompileContext}.
	 * @return {@link AutoWirer} with context for the {@link OfficeFloor}.
	 */
	AutoWirer<ManagedObjectExtensionNode> loadAutoWireExtensionTargets(AutoWirer<ManagedObjectExtensionNode> autoWirer,
			CompileContext compileContext);

	/**
	 * Loads the {@link AutoWire} targets for the {@link OfficeFloorTeam} instances.
	 * 
	 * @param autoWirer          {@link AutoWire} to be loaded with the
	 *                           {@link OfficeFloorTeam} targets.
	 * @param officeTeamRegistry {@link OfficeTeamRegistry}.
	 * @param compileContext     {@link CompileContext}.
	 */
	void loadAutoWireTeamTargets(AutoWirer<LinkTeamNode> autoWirer, OfficeTeamRegistry officeTeamRegistry,
			CompileContext compileContext);

	/**
	 * Loads the {@link OfficeFloorType}.
	 * 
	 * @param compileContext {@link CompileContext}.
	 * @return <code>true</code> if the {@link OfficeFloorType} was loaded.
	 */
	OfficeFloorType loadOfficeFloorType(CompileContext compileContext);

	/**
	 * Obtains the {@link OfficeFloorDeployer} configured
	 * {@link OfficeFloorListener} instances.
	 * 
	 * @return {@link OfficeFloorDeployer} configured {@link OfficeFloorListener}
	 *         instances.
	 */
	OfficeFloorListener[] getOfficeFloorListeners();

	/**
	 * Indicates if default {@link ExecutionStrategy} is being used.
	 * 
	 * @return <code>true</code> if default {@link ExecutionStrategy} is being used.
	 */
	boolean isDefaultExecutionStrategy();

	/**
	 * Deploys the {@link OfficeFloor}.
	 * 
	 * @param officeFloorName    Name of the {@link OfficeFloor}.
	 * @param officeFloorBuilder {@link OfficeFloorBuilder} to build the deployed
	 *                           {@link OfficeFloor}.
	 * @param compileContext     {@link CompileContext}.
	 * @return {@link OfficeFloor}.
	 */
	OfficeFloor deployOfficeFloor(String officeFloorName, OfficeFloorBuilder officeFloorBuilder,
			CompileContext compileContext);

	/**
	 * Loads the {@link FunctionManager} instances to externally trigger this
	 * {@link OfficeFloorNode}.
	 * 
	 * @param officeFloor {@link OfficeFloor} for this {@link OfficeFloorNode}.
	 * @throws UnknownOfficeException   {@link UnknownOfficeException}.
	 * @throws UnknownFunctionException {@link UnknownFunctionException}.
	 */
	void loadExternalServicing(OfficeFloor officeFloor) throws UnknownOfficeException, UnknownFunctionException;

	/**
	 * Obtains the {@link InternalSupplier} instances.
	 * 
	 * @return {@link InternalSupplier} instances.
	 */
	InternalSupplier[] getInternalSuppliers();

}
