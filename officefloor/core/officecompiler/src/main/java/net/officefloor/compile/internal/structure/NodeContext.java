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

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.spi.supplier.source.SupplierSource;
import net.officefloor.autowire.supplier.SupplierLoader;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.administrator.AdministratorLoader;
import net.officefloor.compile.governance.GovernanceLoader;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.office.OfficeLoader;
import net.officefloor.compile.officefloor.OfficeFloorLoader;
import net.officefloor.compile.pool.ManagedObjectPoolLoader;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionLoader;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.team.TeamLoader;
import net.officefloor.compile.work.WorkLoader;
import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.source.SourceContext;
import net.officefloor.frame.spi.team.source.TeamSource;

/**
 * Context for a {@link Node}.
 * 
 * @author Daniel Sagenschneider
 */
public interface NodeContext {

	/**
	 * Obtains the root {@link SourceContext}.
	 * 
	 * @return Root {@link SourceContext}.
	 */
	SourceContext getRootSourceContext();

	/**
	 * Obtains the {@link CompilerIssues}.
	 * 
	 * @return {@link CompilerIssues}.
	 */
	CompilerIssues getCompilerIssues();

	/**
	 * Obtains the {@link OfficeFrame}.
	 * 
	 * @return {@link OfficeFrame}.
	 */
	OfficeFrame getOfficeFrame();

	/**
	 * Initiates the {@link OfficeFloorBuilder} with the
	 * {@link OfficeFloorCompiler} details.
	 * 
	 * @param builder
	 *            {@link OfficeFloorBuilder}.
	 */
	void initiateOfficeFloorBuilder(OfficeFloorBuilder builder);

	/**
	 * Creates a new {@link PropertyList}.
	 * 
	 * @return New {@link PropertyList}.
	 */
	PropertyList createPropertyList();

	/**
	 * Obtains the {@link OfficeFloorSource} class.
	 * 
	 * @param <S>
	 *            {@link OfficeFloorSource} type.
	 * @param officeFloorSourceClassName
	 *            {@link Class} name of the {@link OfficeFloorSource}.
	 * @param node
	 *            {@link Node} requirining the {@link OfficeFloorSource} class.
	 * @return {@link OfficeFloorSource} class, or <code>null</code> with issues
	 *         reported to the {@link CompilerIssues} of this
	 *         {@link NodeContext}.
	 */
	<S extends OfficeFloorSource> Class<S> getOfficeFloorSourceClass(String officeFloorSourceClassName, Node node);

	/**
	 * Obtains the {@link OfficeFloorLoader}.
	 * 
	 * @param node
	 *            {@link Node} requiring the {@link OfficeFloorLoader}.
	 * @return {@link OfficeFloorLoader}.
	 */
	OfficeFloorLoader getOfficeFloorLoader(Node node);

	/**
	 * Creates the {@link OfficeFloorNode}.
	 * 
	 * @param officeFloorSourceClassName
	 *            {@link Class} name of the {@link OfficeFloorSource}.
	 * @param officeFloorSource
	 *            Optional instantiated {@link OfficeFloorSource}. May be
	 *            <code>null</code>.
	 * @param officeFloorLocation
	 *            Location of the {@link OfficeFloor}.
	 * @return {@link OfficeFloorNode}.
	 */
	OfficeFloorNode createOfficeFloorNode(String officeFloorSourceClassName, OfficeFloorSource officeFloorSource,
			String officeFloorLocation);

	/**
	 * Obtains the {@link OfficeSource} class.
	 * 
	 * @param <S>
	 *            {@link OfficeSource} type.
	 * @param officeSourceClassName
	 *            {@link OfficeSource} class name or an alias to an
	 *            {@link OfficeSource} class.
	 * @param node
	 *            {@link Node} requiring the {@link OfficeSource} class.
	 * @return {@link OfficeSource} class, or <code>null</code> with issues
	 *         reported to the {@link CompilerIssues} of this
	 *         {@link NodeContext}.
	 */
	<S extends OfficeSource> Class<S> getOfficeSourceClass(String officeSourceClassName, Node node);

	/**
	 * Obtains the {@link OfficeLoader}.
	 * 
	 * @param node
	 *            {@link Node} requiring the {@link OfficeLoader}.
	 * @return {@link OfficeLoader}.
	 */
	OfficeLoader getOfficeLoader(Node node);

	/**
	 * Creates the {@link OfficeInputNode}.
	 * 
	 * @param officeInputName
	 *            Name of the {@link OfficeInputNode}.
	 * @param office
	 *            Parent {@link OfficeNode}.
	 * @return {@link OfficeInputNode}.
	 */
	OfficeInputNode createOfficeInputNode(String officeInputName, OfficeNode office);

	/**
	 * Creates the {@link OfficeNode}.
	 * 
	 * @param officeName
	 *            Name of the {@link OfficeNode}.
	 * @param officeFloor
	 *            Parent {@link OfficeFloorNode}.
	 * @return {@link OfficeNode}.
	 */
	OfficeNode createOfficeNode(String officeName, OfficeFloorNode officeFloor);

	/**
	 * Creates the {@link OfficeObjectNode}.
	 * 
	 * @param objectName
	 *            Name of the {@link OfficeObjectNode}.
	 * @param office
	 *            Parent {@link OfficeNode}.
	 * @return {@link OfficeObjectNode}.
	 */
	OfficeObjectNode createOfficeObjectNode(String objectName, OfficeNode office);

	/**
	 * Creates the {@link OfficeOutputNode}.
	 * 
	 * @param name
	 *            Name of the {@link OfficeOutputNode}.
	 * @param office
	 *            Parent {@link OfficeNode}.
	 * @return {@link OfficeOutputNode}.
	 */
	OfficeOutputNode createOfficeOutputNode(String name, OfficeNode office);

	/**
	 * Creates the {@link OfficeStartNode}.
	 * 
	 * @param startName
	 *            Name of the {@link OfficeStartNode}.
	 * @param office
	 *            Parent {@link OfficeNode}.
	 * @return {@link OfficeStartNode}.
	 */
	OfficeStartNode createOfficeStartNode(String startName, OfficeNode office);

	/**
	 * Creates the {@link OfficeTeamNode}.
	 * 
	 * @param officeTeamName
	 *            Name of the {@link OfficeTeamNode}.
	 * @param office
	 *            Parent {@link OfficeNode}.
	 * @return {@link OfficeTeamNode}.
	 */
	OfficeTeamNode createOfficeTeamNode(String officeTeamName, OfficeNode office);

	/**
	 * Obtains the {@link SectionSource} class.
	 * 
	 * @param <S>
	 *            {@link SectionSource} type.
	 * @param sectionSourceClassName
	 *            {@link SectionSource} class name or an alias to an
	 *            {@link SectionSource} class.
	 * @param node
	 *            {@link Node} requiring the {@link SectionSource} class.
	 * @return {@link SectionSource} class, or <code>null</code> with issues
	 *         reported to the {@link CompilerIssues} of this
	 *         {@link NodeContext}.
	 */
	<S extends SectionSource> Class<S> getSectionSourceClass(String sectionSourceClassName, Node node);

	/**
	 * Obtains the {@link SectionLoader}.
	 * 
	 * @param node
	 *            {@link Node} requiring the {@link SectionLoader}.
	 * @return {@link SectionLoader}.
	 */
	SectionLoader getSectionLoader(Node node);

	/**
	 * Creates the {@link SectionInputNode}.
	 * 
	 * @param inputName
	 *            Name of the {@link SectionInputNode}.
	 * @param section
	 *            Parent {@link SectionNode}.
	 * @return {@link SectionInputNode}.
	 */
	SectionInputNode createSectionInputNode(String inputName, SectionNode section);

	/**
	 * Creates the {@link SectionObjectNode}.
	 * 
	 * @param objectName
	 *            Name of the {@link SectionObjectNode}.
	 * @param section
	 *            Parent {@link SectionNode}.
	 * @return {@link SectionObjectNode}.
	 */
	SectionObjectNode createSectionObjectNode(String objectName, SectionNode section);

	/**
	 * Creates the {@link SectionOutputNode}.
	 * 
	 * @param outputName
	 *            Name of the {@link SectionOutputNode}.
	 * @param section
	 *            Parent {@link SectionNode}.
	 * @return {@link SectionOutputNode}.
	 */
	SectionOutputNode createSectionOutputNode(String outputName, SectionNode section);

	/**
	 * Creates a top level {@link SectionNode} within the {@link OfficeNode}.
	 * 
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @param office
	 *            {@link OfficeNode} containing this {@link OfficeSection}.
	 * @return {@link SectionNode}.
	 */
	SectionNode createSectionNode(String sectionName, OfficeNode office);

	/**
	 * Creates a {@link SectionNode}.
	 * 
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @param parentSection
	 *            Parent {@link SectionNode} containing this
	 *            {@link OfficeSection}.
	 * @return {@link SectionNode}.
	 */
	SectionNode createSectionNode(String sectionName, SectionNode parentSection);

	/**
	 * Obtains the {@link WorkSource} class.
	 * 
	 * @param <S>
	 *            {@link WorkSource} type.
	 * @param workSourceName
	 *            {@link WorkSource} class name or an alias to a
	 *            {@link WorkSource} class.
	 * @param node
	 *            {@link Node} requiring the {@link WorkSource} class.
	 * @return {@link WorkSource} class, or <code>null</code> with issues
	 *         reported to the {@link CompilerIssues} of this
	 *         {@link NodeContext}.
	 */
	<S extends WorkSource<?>> Class<S> getWorkSourceClass(String workSourceName, Node node);

	/**
	 * Obtains the {@link WorkLoader}.
	 * 
	 * @param node
	 *            {@link Node} requiring the {@link WorkLoader}.
	 * @return {@link WorkLoader}.
	 */
	WorkLoader getWorkLoader(Node node);

	/**
	 * Creates the {@link WorkNode}.
	 * 
	 * @param workName
	 *            Name of the {@link WorkNode}.
	 * @param section
	 *            Parent {@link SectionNode}.
	 * @return {@link WorkNode}.
	 */
	WorkNode createWorkNode(String workName, SectionNode section);

	/**
	 * Creates the {@link TaskFlowNode}.
	 * 
	 * @param flowName
	 *            Name of the {@link TaskFlowNode}.
	 * @param isEscalation
	 *            Indicates if is {@link Escalation}.
	 * @param task
	 *            Parent {@link TaskNode}.
	 * @return {@link TaskFlowNode}.
	 */
	TaskFlowNode createTaskFlowNode(String flowName, boolean isEscalation, TaskNode task);

	/**
	 * Creates the {@link TaskNode}.
	 * 
	 * @param taskName
	 *            Name of the {@link TaskNode}.
	 * @return {@link TaskNode}.
	 */
	TaskNode createTaskNode(String taskName);

	/**
	 * Creates the {@link TaskObjectNode}.
	 * 
	 * @param objectName
	 *            Name of the {@link TaskObjectNode}.
	 * @param taskNode
	 *            Parent {@link TaskNode}.
	 * @return {@link TaskObjectNode}.
	 */
	TaskObjectNode createTaskObjectNode(String objectName, TaskNode taskNode);

	/**
	 * Creates the {@link TaskTeamNode}.
	 * 
	 * @param teamName
	 *            Name of the {@link TaskTeamNode}.
	 * @param task
	 *            Parent {@link TaskNode}.
	 * @return {@link TaskTeamNode}.
	 */
	TaskTeamNode createTaskTeamNode(String teamName, TaskNode task);

	/**
	 * Obtains the {@link ManagedObjectSource} class.
	 * 
	 * @param <S>
	 *            {@link ManagedObjectSource} type.
	 * @param managedObjectSourceName
	 *            {@link ManagedObjectSource} class name or an alias to a
	 *            {@link ManagedObjectSource} class.
	 * @param node
	 *            {@link Node} for reporting issues.
	 * @return {@link ManagedObjectSource} class, or <code>null</code> with
	 *         issues reported to the {@link CompilerIssues} of this
	 *         {@link NodeContext}.
	 */
	<S extends ManagedObjectSource<?, ?>> Class<S> getManagedObjectSourceClass(String managedObjectSourceName,
			Node node);

	/**
	 * Obtains the {@link ManagedObjectLoader}.
	 * 
	 * @param node
	 *            {@link Node} requiring the {@link ManagedObjectLoader}.
	 * @return {@link ManagedObjectLoader}.
	 */
	ManagedObjectLoader getManagedObjectLoader(Node node);

	/**
	 * Creates an {@link InputManagedObjectNode}.
	 * 
	 * @param inputManagedObjectName
	 *            Name of the {@link InputManagedObjectNode}.
	 * @param officeFloor
	 *            Parent {@link OfficeFloorNode}.
	 * @return {@link InputManagedObjectNode}.
	 */
	InputManagedObjectNode createInputManagedNode(String inputManagedObjectName, OfficeFloorNode officeFloor);

	/**
	 * Creates a {@link ManagedObjectDependencyNode} for a
	 * {@link ManagedObjectNode}.
	 * 
	 * @param dependencyName
	 *            Name of the {@link ManagedObjectDependencyNode}.
	 * @param managedObject
	 *            Parent {@link ManagedObjectNode}.
	 * @return {@link ManagedObjectDependencyNode}.
	 */
	ManagedObjectDependencyNode createManagedObjectDependencyNode(String dependencyName,
			ManagedObjectNode managedObject);

	/**
	 * Creates a {@link ManagedObjectDependencyNode} for a
	 * {@link InputManagedObjectNode}.
	 * 
	 * @param dependencyName
	 *            Name of the {@link ManagedObjectDependencyNode}.
	 * @param managedObjectSource
	 *            Parent {@link ManagedObjectSourceNode}.
	 * @return {@link ManagedObjectDependencyNode}.
	 */
	ManagedObjectDependencyNode createManagedObjectDependencyNode(String dependencyName,
			ManagedObjectSourceNode managedObjectSource);

	/**
	 * Creates the {@link ManagedObjectFlowNode}.
	 * 
	 * @param flowName
	 *            Name of the {@link ManagedObjectFlowNode}.
	 * @param managedObjectSource
	 *            Parent {@link ManagedObjectSourceNode}.
	 * @return {@link ManagedObjectFlowNode}.
	 */
	ManagedObjectFlowNode createManagedObjectFlowNode(String flowName, ManagedObjectSourceNode managedObjectSource);

	/**
	 * Creates the {@link ManagedObjectTeamNode}.
	 * 
	 * @param teamName
	 *            Name of the {@link ManagedObjectTeamNode}.
	 * @param managedObjectSource
	 *            Parent {@link ManagedObjectSourceNode}.
	 * @return {@link ManagedObjectTeamNode}.
	 */
	ManagedObjectTeamNode createManagedObjectTeamNode(String teamName, ManagedObjectSourceNode managedObjectSource);

	/**
	 * Creates the {@link ManagingOfficeNode}.
	 * 
	 * @param managedObjectSource
	 *            Parent {@link ManagedObjectSourceNode}.
	 * @return {@link ManagingOfficeNode}.
	 */
	ManagingOfficeNode createManagingOfficeNode(ManagedObjectSourceNode managedObjectSource);

	/**
	 * Creates the {@link ManagedObjectNode}.
	 * 
	 * @param managedObjectName
	 *            Name of the {@link ManagedObjectNode}.
	 * @return {@link ManagedObjectNode}.
	 */
	ManagedObjectNode createManagedObjectNode(String managedObjectName);

	/**
	 * Creates a {@link ManagedObjectSourceNode}.
	 * 
	 * @param managedObjectSourceName
	 *            Name of the {@link ManagedObjectSourceNode}.
	 * @param section
	 *            Parent {@link SectionNode}.
	 * @return {@link ManagedObjectSourceNode}.
	 */
	ManagedObjectSourceNode createManagedObjectSourceNode(String managedObjectSourceName, SectionNode section);

	/**
	 * Creates a {@link ManagedObjectSourceNode}.
	 * 
	 * @param managedObjectSourceName
	 *            Name of the {@link ManagedObjectSourceNode}.
	 * @param office
	 *            Parent {@link OfficeNode}.
	 * @return {@link ManagedObjectSourceNode}.
	 */
	ManagedObjectSourceNode createManagedObjectSourceNode(String managedObjectSourceName, OfficeNode office);

	/**
	 * Creates a {@link ManagedObjectSourceNode}.
	 * 
	 * @param managedObjectSourceName
	 *            Name of the {@link ManagedObjectSourceNode}.
	 * @param suppliedManagedObject
	 *            Parent {@link SuppliedManagedObjectNode}.
	 * @return {@link ManagedObjectSourceNode}.
	 */
	ManagedObjectSourceNode createManagedObjectSourceNode(String managedObjectSourceName,
			SuppliedManagedObjectNode suppliedManagedObject);

	/**
	 * Creates a {@link ManagedObjectSourceNode}.
	 * 
	 * @param managedObjectSourceName
	 *            Name of the {@link ManagedObjectSourceNode}.
	 * @param officeFloor
	 *            Parent {@link OfficeFloorNode}.
	 * @return {@link ManagedObjectSourceNode}.
	 */
	ManagedObjectSourceNode createManagedObjectSourceNode(String managedObjectSourceName, OfficeFloorNode officeFloor);

	/**
	 * Obtains the {@link ManagedObjectPoolLoader}.
	 * 
	 * @param node
	 *            {@link Node} requiring the {@link ManagedObjectPoolLoader}.
	 * @return {@link ManagedObjectPoolLoader}.
	 */
	ManagedObjectPoolLoader getManagedObjectPoolLoader(Node node);

	/**
	 * Obtains the {@link SupplierSource} class.
	 * 
	 * @param <S>
	 *            {@link SupplierSource} type.
	 * @param supplierSourceClassName
	 *            {@link SupplierSource} class name or an alias to a
	 *            {@link SupplierSource} class.
	 * @param node
	 *            {@link Node} requiring the {@link SupplierSource} class.
	 * @return {@link SupplierSource} class, or <code>null</code> with issues
	 *         reported to the {@link CompilerIssues} of this
	 *         {@link NodeContext}.
	 */
	<S extends SupplierSource> Class<S> getSupplierSourceClass(String supplierSourceClassName, Node node);

	/**
	 * Obtains the {@link SupplierLoader}.
	 * 
	 * @param node
	 *            {@link Node} requiring the {@link SupplierLoader}.
	 * @return {@link SupplierLoader}.
	 */
	SupplierLoader getSupplierLoader(Node node);

	/**
	 * Creates the {@link SuppliedManagedObjectNode}.
	 * 
	 * @param autoWire
	 *            {@link AutoWire}.
	 * @param supplier
	 *            Parent {@link SupplierNode}.
	 * @return {@link SuppliedManagedObjectNode}.
	 */
	SuppliedManagedObjectNode createSuppliedManagedObjectNode(AutoWire autoWire, SupplierNode supplier);

	/**
	 * Creates the {@link SupplierNode}.
	 * 
	 * @param supplierName
	 *            Name of the {@link SupplierNode}.
	 * @param supplierSourceClassName
	 *            {@link Class} name of the {@link SupplierSource}.
	 * @param officeFloor
	 *            Parent {@link OfficeFloorNode}.
	 * @return {@link SupplierNode}.
	 */
	SupplierNode createSupplierNode(String supplierName, String supplierSourceClassName, OfficeFloorNode officeFloor);

	/**
	 * Obtains the {@link AdministratorSource} class.
	 * 
	 * @param <S>
	 *            {@link AdministratorSource} type.
	 * @param administratorSourceClassName
	 *            {@link AdministratorSource} class name or an alias to an
	 *            {@link AdministratorSource} class.
	 * @param node
	 *            {@link Node} requiring the {@link AdministratorSource} class.
	 * @return {@link AdministratorSource} class, or <code>null</code> with
	 *         issues reported to the {@link CompilerIssues} of this
	 *         {@link NodeContext}.
	 */
	<S extends AdministratorSource<?, ?>> Class<S> getAdministratorSourceClass(String administratorSourceClassName,
			Node node);

	/**
	 * Obtains the {@link AdministratorLoader}.
	 * 
	 * @param node
	 *            {@link Node} requiring the {@link AdministratorLoader}.
	 * @return {@link AdministratorLoader}.
	 */
	AdministratorLoader getAdministratorLoader(Node node);

	/**
	 * Creates a {@link AdministratorNode}.
	 * 
	 * @param administratorName
	 *            Name of the {@link Administrator}.
	 * @param office
	 *            {@link OfficeNode} containing this {@link Administrator}.
	 * @return {@link AdministratorNode}.
	 */
	AdministratorNode createAdministratorNode(String administratorName, OfficeNode office);

	/**
	 * Creates a {@link DutyNode}.
	 * 
	 * @param dutyName
	 *            Name of the {@link Duty}.
	 * @param administrator
	 *            {@link AdministratorNode}.
	 * @return {@link DutyNode}.
	 */
	DutyNode createDutyNode(String dutyName, AdministratorNode administrator);

	/**
	 * Obtains the {@link GovernanceSource} class.
	 * 
	 * @param <S>
	 *            {@link GovernanceSource} type.
	 * @param governanceSourceName
	 *            {@link GovernanceSource} class name or an alias to an
	 *            {@link GovernanceSource} class.
	 * @param node
	 *            {@link Node} requiring the {@link GovernanceSource} class.
	 * @return {@link GovernanceSource} class, or <code>null</code> with issues
	 *         reported to the {@link CompilerIssues} of this
	 *         {@link NodeContext}.
	 */
	<S extends GovernanceSource<?, ?>> Class<S> getGovernanceSourceClass(String governanceSourceName, Node node);

	/**
	 * Obtains the {@link GovernanceLoader}.
	 * 
	 * @param node
	 *            {@link Node} requiring the {@link GovernanceLoader}.
	 * @return {@link GovernanceLoader}.
	 */
	GovernanceLoader getGovernanceLoader(Node node);

	/**
	 * Creates a {@link GovernanceNode}.
	 * 
	 * @param governanceName
	 *            Name of the {@link Governance}.
	 * @param office
	 *            {@link OfficeNode} containing this {@link Governance}.
	 * @return {@link GovernanceNode}.
	 */
	GovernanceNode createGovernanceNode(String governanceName, OfficeNode office);

	/**
	 * Obtains the {@link TeamSource} class.
	 * 
	 * @param <S>
	 *            {@link TeamSource} type.
	 * @param teamSourceClassName
	 *            {@link TeamSource} class name or an alias to a
	 *            {@link TeamSource} class.
	 * @param node
	 *            {@link Node} requiring the {@link TeamSource} class.
	 * @return {@link TeamSource} class, or <code>null</code> with issues
	 *         reported to the {@link CompilerIssues} of this
	 *         {@link NodeContext}.
	 */
	<S extends TeamSource> Class<S> getTeamSourceClass(String teamSourceClassName, Node node);

	/**
	 * Obtains the {@link TeamLoader}.
	 * 
	 * @param node
	 *            {@link Node} requiring the {@link TeamLoader}.
	 * @return {@link TeamLoader}.
	 */
	TeamLoader getTeamLoader(Node node);

	/**
	 * Creates the {@link TeamNode}.
	 * 
	 * @param teamName
	 *            Name of the {@link TeamNode}.
	 * @param officeFloor
	 *            Parent {@link OfficeFloorNode}.
	 * @return {@link TeamNode}.
	 */
	TeamNode createTeamNode(String teamName, OfficeFloorNode officeFloor);

	/**
	 * Creates an {@link EscalationNode}.
	 * 
	 * @param escalationType
	 *            {@link Escalation} type.
	 * @param officeNode
	 *            {@link OfficeNode} containing this {@link Escalation}.
	 * @return {@link EscalationNode}.
	 */
	EscalationNode createEscalationNode(String escalationType, OfficeNode officeNode);

}