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

package net.officefloor.compile.spi.officefloor;

import net.officefloor.compile.issues.SourceIssues;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSource;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.frame.api.build.OfficeFloorListener;
import net.officefloor.frame.api.executive.source.ExecutiveSource;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;

/**
 * Deploys the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorDeployer extends SourceIssues {

	/**
	 * Flags to attempt to auto wire any non-configured object links.
	 */
	void enableAutoWireObjects();

	/**
	 * Flags to attempt to auto wire any non-configured {@link Team} links.
	 */
	void enableAutoWireTeams();

	/**
	 * <p>
	 * Adds an {@link OfficeFloorListener}.
	 * <p>
	 * This enables external services to operate in the open/close life-cycle of
	 * {@link OfficeFloor}.
	 * 
	 * @param listener {@link OfficeFloorListener}.
	 */
	void addOfficeFloorListener(OfficeFloorListener listener);

	/**
	 * Adds a {@link ManagedObjectSourceAugmentor}.
	 * 
	 * @param managedObjectSourceAugmentor {@link ManagedObjectSourceAugmentor}.
	 */
	void addManagedObjectSourceAugmentor(ManagedObjectSourceAugmentor managedObjectSourceAugmentor);

	/**
	 * Adds a {@link TeamAugmentor}.
	 * 
	 * @param teamAugmentor {@link TeamAugmentor}.
	 */
	void addTeamAugmentor(TeamAugmentor teamAugmentor);

	/**
	 * Adds a {@link OfficeFloorTeam}.
	 * 
	 * @param teamName            Name of the {@link OfficeFloorTeam}.
	 * @param teamSourceClassName Fully qualified class name of the
	 *                            {@link TeamSource}.
	 * @return Added {@link OfficeFloorTeam}.
	 */
	OfficeFloorTeam addTeam(String teamName, String teamSourceClassName);

	/**
	 * Adds a {@link OfficeFloorTeam}.
	 * 
	 * @param teamName   Name of the {@link OfficeFloorTeam}.
	 * @param teamSource {@link TeamSource}.
	 * @return Added {@link OfficeFloorTeam}.
	 */
	OfficeFloorTeam addTeam(String teamName, TeamSource teamSource);

	/**
	 * Sets the {@link OfficeFloorExecutive}.
	 * 
	 * @param executiveSourceClassName Fully qualified class name of the
	 *                                 {@link ExecutiveSource}.
	 * @return Set {@link OfficeFloorExecutive}.
	 */
	OfficeFloorExecutive setExecutive(String executiveSourceClassName);

	/**
	 * Sets the {@link OfficeFloorExecutive}.
	 * 
	 * @param executiveSource {@link ExecutiveSource}.
	 * @return Set {@link OfficeFloorExecutive}.
	 */
	OfficeFloorExecutive setExecutive(ExecutiveSource executiveSource);

	/**
	 * Adds an {@link OfficeFloorManagedObjectSource}.
	 * 
	 * @param managedObjectSourceName      Name of the
	 *                                     {@link OfficeFloorManagedObjectSource}.
	 * @param managedObjectSourceClassName Fully qualified class name of the
	 *                                     {@link ManagedObjectSource}.
	 * @return Added {@link OfficeFloorManagedObjectSource}.
	 */
	OfficeFloorManagedObjectSource addManagedObjectSource(String managedObjectSourceName,
			String managedObjectSourceClassName);

	/**
	 * Adds an {@link OfficeFloorManagedObjectSource}.
	 * 
	 * @param managedObjectSourceName Name of the
	 *                                {@link OfficeFloorManagedObjectSource}.
	 * @param managedObjectSource     {@link ManagedObjectSource} instance to use.
	 * @return Added {@link OfficeFloorManagedObjectSource}.
	 */
	OfficeFloorManagedObjectSource addManagedObjectSource(String managedObjectSourceName,
			ManagedObjectSource<?, ?> managedObjectSource);

	/**
	 * Adds an {@link OfficeFloorInputManagedObject}.
	 * 
	 * @param inputManagedObjectName Name of the
	 *                               {@link OfficeFloorInputManagedObject}.
	 * @param inputObjectType        Input {@link Object} type.
	 * @return Added {@link OfficeFloorInputManagedObject}.
	 */
	OfficeFloorInputManagedObject addInputManagedObject(String inputManagedObjectName, String inputObjectType);

	/**
	 * Adds an {@link OfficeFloorManagedObjectPool}.
	 * 
	 * @param managedObjectPoolName            Name of the
	 *                                         {@link OfficeFloorManagedObjectPool}.
	 * @param managedObjectPoolSourceClassName Fully qualified class name of the
	 *                                         {@link ManagedObjectPoolSource}.
	 * @return Added {@link OfficeFloorManagedObjectPool}.
	 */
	OfficeFloorManagedObjectPool addManagedObjectPool(String managedObjectPoolName,
			String managedObjectPoolSourceClassName);

	/**
	 * Adds an {@link OfficeFloorManagedObjectPool}.
	 * 
	 * @param managedObjectPoolName   Name of the
	 *                                {@link OfficeFloorManagedObjectPool}.
	 * @param managedObjectPoolSource {@link ManagedObjectPoolSource} instance to
	 *                                use.
	 * @return {@link OfficeFloorManagedObjectPool}.
	 */
	OfficeFloorManagedObjectPool addManagedObjectPool(String managedObjectPoolName,
			ManagedObjectPoolSource managedObjectPoolSource);

	/**
	 * Adds an {@link OfficeFloorSupplier}.
	 * 
	 * @param supplierName            Name of the {@link OfficeFloorSupplier}.
	 * @param supplierSourceClassName Fully qualified class name of the
	 *                                {@link SupplierSource}.
	 * @return {@link OfficeFloorSupplier}.
	 */
	OfficeFloorSupplier addSupplier(String supplierName, String supplierSourceClassName);

	/**
	 * Adds an {@link OfficeFloorSupplier}.
	 * 
	 * @param supplierName   Name of the {@link OfficeFloorSupplier}.
	 * @param supplierSource {@link SupplierSource} instance to use.
	 * @return {@link OfficeFloorSupplier}.
	 */
	OfficeFloorSupplier addSupplier(String supplierName, SupplierSource supplierSource);

	/**
	 * Adds a {@link DeployedOffice} to the {@link OfficeFloor}.
	 * 
	 * @param officeName            Name of the {@link Office}.
	 * @param officeSourceClassName Fully qualified class name of the
	 *                              {@link OfficeSource}.
	 * @param officeLocation        Location of the {@link Office}.
	 * @return {@link DeployedOffice}.
	 */
	DeployedOffice addDeployedOffice(String officeName, String officeSourceClassName, String officeLocation);

	/**
	 * Adds a {@link DeployedOffice} to the {@link OfficeFloor}.
	 * 
	 * @param officeName     Name of the {@link Office}.
	 * @param officeSource   {@link OfficeSource} instance.
	 * @param officeLocation Location of the {@link Office}.
	 * @return {@link DeployedOffice}.
	 */
	DeployedOffice addDeployedOffice(String officeName, OfficeSource officeSource, String officeLocation);

	/**
	 * Obtains the {@link DeployedOffice}.
	 * 
	 * @param officeName Name of the {@link DeployedOffice}.
	 * @return {@link DeployedOffice}.
	 */
	DeployedOffice getDeployedOffice(String officeName);

	/**
	 * Obtains the currently added {@link DeployedOffice} instances.
	 * 
	 * @return Currently added {@link DeployedOffice} instances.
	 */
	DeployedOffice[] getDeployedOffices();

	/**
	 * Links the {@link OfficeFloorInputManagedObject} to be input by the
	 * {@link OfficeFloorManagedObjectSource}.
	 * 
	 * @param managedObjectSource {@link OfficeFloorManagedObjectSource}.
	 * @param inputManagedObject  {@link OfficeFloorInputManagedObject}.
	 */
	void link(OfficeFloorManagedObjectSource managedObjectSource, OfficeFloorInputManagedObject inputManagedObject);

	/**
	 * Links the {@link OfficeFloorManagedObjectSource} to be pooled by the
	 * {@link OfficeFloorManagedObjectPool}.
	 * 
	 * @param managedObjectSource {@link OfficeFloorManagedObject}.
	 * @param managedObjectPool   {@link OfficeFloorManagedObjectPool}.
	 */
	void link(OfficeFloorManagedObjectSource managedObjectSource, OfficeFloorManagedObjectPool managedObjectPool);

	/**
	 * Links the {@link OfficeFloorFlowSourceNode} to be undertake by the
	 * {@link OfficeFloorFlowSinkNode}.
	 * 
	 * @param flowSourceNode {@link OfficeFloorFlowSourceNode}.
	 * @param flowSinkNode   {@link OfficeFloorFlowSinkNode}.
	 */
	void link(OfficeFloorFlowSourceNode flowSourceNode, OfficeFloorFlowSinkNode flowSinkNode);

	/**
	 * Links the {@link OfficeFloorDependencyObjectNode} to be fulfilled by the
	 * {@link OfficeFloorDependencyObjectNode}.
	 * 
	 * @param dependencyRequireNode {@link OfficeFloorDependencyRequireNode}.
	 * @param dependencyObjectNode  {@link OfficeFloorDependencyObjectNode}.
	 */
	void link(OfficeFloorDependencyRequireNode dependencyRequireNode,
			OfficeFloorDependencyObjectNode dependencyObjectNode);

	/**
	 * Links the {@link OfficeFloorResponsibility} to the {@link OfficeFloorTeam}.
	 * 
	 * @param responsibility  {@link OfficeFloorResponsibility}.
	 * @param officeFloorTeam {@link OfficeFloorTeam}.
	 */
	void link(OfficeFloorResponsibility responsibility, OfficeFloorTeam officeFloorTeam);

	/**
	 * Links the {@link OfficeFloorManagedObjectExecutionStrategy} to the
	 * {@link OfficeFloorExecutionStrategy}.
	 * 
	 * @param managedObjectExecutionStrategy {@link OfficeFloorManagedObjectExecutionStrategy}.
	 * @param executionStrategy              {@link OfficeFloorExecutionStrategy}.
	 */
	void link(OfficeFloorManagedObjectExecutionStrategy managedObjectExecutionStrategy,
			OfficeFloorExecutionStrategy executionStrategy);

	/**
	 * Links the {@link OfficeFloorTeam} to its {@link OfficeFloorTeamOversight}.
	 * 
	 * @param team      {@link OfficeFloorTeam}.
	 * @param oversight {@link OfficeFloorTeamOversight}.
	 */
	void link(OfficeFloorTeam team, OfficeFloorTeamOversight oversight);

	/**
	 * Links the {@link ManagingOffice} to be managed by the {@link DeployedOffice}.
	 * 
	 * @param managingOffice {@link ManagingOffice}.
	 * @param office         {@link DeployedOffice}.
	 */
	void link(ManagingOffice managingOffice, DeployedOffice office);

}
