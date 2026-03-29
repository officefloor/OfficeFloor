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

package net.officefloor.compile.spi.office;

import net.officefloor.compile.issues.SourceIssues;
import net.officefloor.compile.spi.administration.source.AdministrationSource;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorSupplier;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSource;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.team.Team;

/**
 * Architect to structure the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeArchitect extends SourceIssues {

	/**
	 * Flags to attempt to auto wire any non-configured object links.
	 */
	void enableAutoWireObjects();

	/**
	 * Flags to attempt to auto wire any non-configured {@link Team} links.
	 */
	void enableAutoWireTeams();

	/**
	 * Adds a {@link OfficeInput}.
	 * 
	 * @param inputName     Name of the {@link OfficeInput}.
	 * @param parameterType Fully qualified type name of the parameter to this
	 *                      {@link OfficeInput}.
	 * @return Added {@link OfficeInput}.
	 */
	OfficeInput addOfficeInput(String inputName, String parameterType);

	/**
	 * Adds a {@link OfficeOutput}.
	 * 
	 * @param outputName   Name of the {@link OfficeOutput}.
	 * @param argumentType Fully qualified type name of the argument from this
	 *                     {@link OfficeOutput}.
	 * @return Added {@link OfficeOutput}.
	 */
	OfficeOutput addOfficeOutput(String outputName, String argumentType);

	/**
	 * Adds an {@link OfficeObject}.
	 * 
	 * @param officeObjectName Name of the {@link OfficeObject}.
	 * @param objectType       Object type.
	 * @return Added {@link OfficeObject}.
	 */
	OfficeObject addOfficeObject(String officeObjectName, String objectType);

	/**
	 * Adds an {@link OfficeTeam}.
	 * 
	 * @param officeTeamName Name of the {@link OfficeTeam}.
	 * @return Added {@link OfficeTeam}.
	 */
	OfficeTeam addOfficeTeam(String officeTeamName);

	/**
	 * Adds an {@link OfficeSection}.
	 * 
	 * @param sectionName            Name of the {@link OfficeSection}.
	 * @param sectionSourceClassName Fully qualified class name of the
	 *                               {@link SectionSource}.
	 * @param sectionLocation        Location of the {@link OfficeSection}.
	 * @return Added {@link OfficeSection}.
	 */
	OfficeSection addOfficeSection(String sectionName, String sectionSourceClassName, String sectionLocation);

	/**
	 * Adds an {@link OfficeSection}.
	 * 
	 * @param sectionName     Name of the {@link OfficeSection}.
	 * @param sectionSource   {@link SectionSource} instance to use.
	 * @param sectionLocation Location of the {@link OfficeSection}.
	 * @return Added {@link OfficeSection}.
	 */
	OfficeSection addOfficeSection(String sectionName, SectionSource sectionSource, String sectionLocation);

	/**
	 * Obtains the {@link OfficeSection}.
	 * 
	 * @param sectionName Name of the {@link OfficeSection}.
	 * @return {@link OfficeSection}.
	 */
	OfficeSection getOfficeSection(String sectionName);

	/**
	 * Adds an {@link OfficeSectionTransformer} to transform the
	 * {@link OfficeSection} instances of the {@link Office}.
	 * 
	 * @param transformer {@link OfficeSectionTransformer}.
	 */
	void addOfficeSectionTransformer(OfficeSectionTransformer transformer);

	/**
	 * Adds a {@link ManagedFunctionAugmentor}.
	 * 
	 * @param managedFunctionAugmentor {@link ManagedFunctionAugmentor}.
	 */
	void addManagedFunctionAugmentor(ManagedFunctionAugmentor managedFunctionAugmentor);

	/**
	 * Adds a {@link OfficeManagedObjectSource}.
	 * 
	 * @param managedObjectSourceName      Name of the
	 *                                     {@link OfficeManagedObjectSource}.
	 * @param managedObjectSourceClassName Fully qualified class name of the
	 *                                     {@link ManagedObjectSource}.
	 * @return Added {@link OfficeManagedObjectSource}.
	 */
	OfficeManagedObjectSource addOfficeManagedObjectSource(String managedObjectSourceName,
			String managedObjectSourceClassName);

	/**
	 * Adds a {@link OfficeManagedObjectSource}.
	 * 
	 * @param managedObjectSourceName Name of the {@link OfficeManagedObjectSource}.
	 * @param managedObjectSource     {@link ManagedObjectSource} instance to use.
	 * @return Added {@link OfficeManagedObjectSource}.
	 */
	OfficeManagedObjectSource addOfficeManagedObjectSource(String managedObjectSourceName,
			ManagedObjectSource<?, ?> managedObjectSource);

	/**
	 * Adds an {@link OfficeManagedObjectPool}.
	 * 
	 * @param managedObjectPoolName            Name of the
	 *                                         {@link OfficeManagedObjectPool}.
	 * @param managedObjectPoolSourceClassName Fully qualified class name of the
	 *                                         {@link ManagedObjectPoolSource}.
	 * @return Added {@link OfficeManagedObjectPool}.
	 */
	OfficeManagedObjectPool addManagedObjectPool(String managedObjectPoolName, String managedObjectPoolSourceClassName);

	/**
	 * Adds an {@link OfficeManagedObjectPool}.
	 * 
	 * @param managedObjectPoolName   Name of the {@link OfficeManagedObjectPool}.
	 * @param managedObjectPoolSource {@link ManagedObjectPoolSource} instance to
	 *                                use.
	 * @return {@link OfficeManagedObjectPool}.
	 */
	OfficeManagedObjectPool addManagedObjectPool(String managedObjectPoolName,
			ManagedObjectPoolSource managedObjectPoolSource);

	/**
	 * Adds an {@link OfficeSupplier}.
	 * 
	 * @param supplierName            Name of the {@link OfficeSupplier}.
	 * @param supplierSourceClassName Fully qualified class name of the
	 *                                {@link SupplierSource}.
	 * @return {@link OfficeSupplier}.
	 */
	OfficeSupplier addSupplier(String supplierName, String supplierSourceClassName);

	/**
	 * Adds an {@link OfficeSupplier}.
	 * 
	 * @param supplierName   Name of the {@link OfficeSupplier}.
	 * @param supplierSource {@link SupplierSource} instance to use.
	 * @return {@link OfficeFloorSupplier}.
	 */
	OfficeSupplier addSupplier(String supplierName, SupplierSource supplierSource);

	/**
	 * Adds a {@link OfficeGovernance}.
	 * 
	 * @param governanceName            Name of the {@link OfficeGovernance}.
	 * @param governanceSourceClassName Fully qualified class name of the
	 *                                  {@link GovernanceSource}.
	 * @return Added {@link OfficeGovernance}.
	 */
	OfficeGovernance addOfficeGovernance(String governanceName, String governanceSourceClassName);

	/**
	 * Adds an {@link OfficeGovernance}.
	 * 
	 * @param governanceName   Name of the {@link OfficeGovernance}.
	 * @param governanceSource {@link GovernanceSource} instance to use.
	 * @return Added {@link OfficeGovernance}.
	 */
	OfficeGovernance addOfficeGovernance(String governanceName, GovernanceSource<?, ?> governanceSource);

	/**
	 * Adds an {@link OfficeAdministration}.
	 * 
	 * @param administrationName            Name of the
	 *                                      {@link OfficeAdministration}.
	 * @param administrationSourceClassName Fully qualified class name of the
	 *                                      {@link AdministrationSource}.
	 * @return Added {@link OfficeAdministration}.
	 */
	OfficeAdministration addOfficeAdministration(String administrationName, String administrationSourceClassName);

	/**
	 * Adds an {@link OfficeAdministration}.
	 * 
	 * @param administrationName   Name of the {@link OfficeAdministration}.
	 * @param administrationSource {@link AdministrationSource} instance to use.
	 * @return Added {@link OfficeAdministration}.
	 */
	OfficeAdministration addOfficeAdministration(String administrationName,
			AdministrationSource<?, ?, ?> administrationSource);

	/**
	 * Adds an {@link OfficeEscalation}.
	 * 
	 * @param escalationTypeName Type of {@link Escalation}.
	 * @return Added {@link OfficeEscalation}.
	 */
	OfficeEscalation addOfficeEscalation(String escalationTypeName);

	/**
	 * Adds an {@link EscalationExplorer} for the execution tree from the added
	 * {@link OfficeEscalation} instances.
	 * 
	 * @param escalationExplorer {@link EscalationExplorer}.
	 */
	void addOfficeEscalationExplorer(EscalationExplorer escalationExplorer);

	/**
	 * Adds a {@link CompletionExplorer} to be notified of completion of exploring
	 * the execution tree.
	 * 
	 * @param completionExplorer {@link CompletionExplorer}.
	 */
	void addOfficeCompletionExplorer(CompletionExplorer completionExplorer);

	/**
	 * Adds an {@link OfficeStart}.
	 * 
	 * @param startName Name of the {@link OfficeStart}.
	 * @return Added {@link OfficeStart}.
	 */
	OfficeStart addOfficeStart(String startName);

	/**
	 * Links the {@link OfficeManagedObjectSource} to be pooled by the
	 * {@link OfficeManagedObjectPool}.
	 * 
	 * @param managedObjectSource {@link OfficeManagedObjectSource}.
	 * @param managedObjectPool   {@link OfficeManagedObjectPool}.
	 */
	void link(OfficeManagedObjectSource managedObjectSource, OfficeManagedObjectPool managedObjectPool);

	/**
	 * Links the {@link OfficeFlowSourceNode} to be undertaken by the
	 * {@link OfficeFlowSinkNode}.
	 * 
	 * @param flowSourceNode {@link OfficeFlowSourceNode}.
	 * @param flowSinkNode   {@link OfficeFlowSinkNode}.
	 */
	void link(OfficeFlowSourceNode flowSourceNode, OfficeFlowSinkNode flowSinkNode);

	/**
	 * Links the {@link OfficeDependencyRequireNode} to be fulfilled by the
	 * {@link OfficeDependencyObjectNode}.
	 * 
	 * @param dependencyRequiredNode {@link OfficeDependencyRequireNode}.
	 * @param dependencyObjectNode   {@link OfficeDependencyObjectNode}.
	 */
	void link(OfficeDependencyRequireNode dependencyRequiredNode, OfficeDependencyObjectNode dependencyObjectNode);

	/**
	 * Links the {@link OfficeResponsibility} to the {@link OfficeTeam}.
	 * 
	 * @param responsibility {@link OfficeResponsibility}.
	 * @param officeTeam     {@link OfficeTeam}.
	 */
	void link(OfficeResponsibility responsibility, OfficeTeam officeTeam);

	/**
	 * Flags an {@link OfficeManagedObjectSource} to be started before another
	 * {@link OfficeManagedObjectSource}.
	 * 
	 * @param startEarlier {@link OfficeManagedObjectSource} to be started up
	 *                     before.
	 * @param startLater   {@link OfficeManagedObjectSource} to be started up
	 *                     afterwards.
	 */
	void startBefore(OfficeManagedObjectSource startEarlier, OfficeManagedObjectSource startLater);

	/**
	 * Flags an {@link OfficeManagedObjectSource} to be started before
	 * {@link ManagedObjectSource} instances providing the type.
	 * 
	 * @param managedObjectSource   {@link OfficeManagedObjectSource} to be started
	 *                              up before.
	 * @param managedObjectTypeName Fully qualified type name of
	 *                              {@link ManagedObject} object type for the
	 *                              {@link ManagedObjectSource} to be started up
	 *                              afterwards.
	 */
	void startBefore(OfficeManagedObjectSource managedObjectSource, String managedObjectTypeName);

	/**
	 * Flags an {@link OfficeManagedObjectSource} to be started after another
	 * {@link OfficeManagedObjectSource}.
	 * 
	 * @param startLater   {@link OfficeManagedObjectSource} to be started up
	 *                     afterwards.
	 * @param startEarlier {@link OfficeManagedObjectSource} to be started up
	 *                     before.
	 */
	void startAfter(OfficeManagedObjectSource startLater, OfficeManagedObjectSource startEarlier);

	/**
	 * Flags an {@link OfficeManagedObjectSource} to be started after
	 * {@link ManagedObjectSource} instances providing the type.
	 * 
	 * @param managedObjectSource   {@link OfficeManagedObjectSource} to be started
	 *                              up after.
	 * @param managedObjectTypeName Fully qualified type name of
	 *                              {@link ManagedObject} object type for the
	 *                              {@link ManagedObjectSource} to be started up
	 *                              beforehand.
	 */
	void startAfter(OfficeManagedObjectSource managedObjectSource, String managedObjectTypeName);

}
