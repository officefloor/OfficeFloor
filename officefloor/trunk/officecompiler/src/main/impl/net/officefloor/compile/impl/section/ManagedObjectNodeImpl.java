/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.compile.impl.section;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectDependencyNode;
import net.officefloor.compile.internal.structure.ManagedObjectFlowNode;
import net.officefloor.compile.internal.structure.ManagedObjectNode;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * {@link ManagedObjectNode} implementation.
 * 
 * @author Daniel
 */
public class ManagedObjectNodeImpl implements ManagedObjectNode {

	/**
	 * Name of this {@link SectionManagedObject}.
	 */
	private final String managedObjectName;

	/**
	 * Class name of the {@link ManagedObjectSource}.
	 */
	private final String managedObjectSourceClassName;

	/**
	 * {@link ManagedObjectDependencyNode} instances by their
	 * {@link ManagedObjectDependency} names.
	 */
	private final Map<String, ManagedObjectDependencyNode> depedencies = new HashMap<String, ManagedObjectDependencyNode>();

	/**
	 * {@link ManagedObjectFlowNode} instances by their
	 * {@link ManagedObjectFlow} names.
	 */
	private final Map<String, ManagedObjectFlowNode> flows = new HashMap<String, ManagedObjectFlowNode>();

	/**
	 * Location of the {@link OfficeSection} containing this
	 * {@link SectionManagedObject}.
	 */
	private final String sectionLocation;

	/**
	 * {@link CompilerIssues}.
	 */
	private final CompilerIssues issues;

	/**
	 * {@link ManagedObjectSource}.
	 */
	private ManagedObjectSource<?, ?> managedObjectSource;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectName
	 *            Name of this {@link SectionManagedObject}.
	 * @param managedObjectSourceClassName
	 *            Class name of the {@link ManagedObjectSource}.
	 * @param managedObjectSource
	 *            {@link ManagedObjectSource}.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection} containing this
	 *            {@link SectionManagedObject}.
	 * @param issues
	 *            {@link CompilerIssues}.
	 */
	public ManagedObjectNodeImpl(String managedObjectName,
			String managedObjectSourceClassName,
			ManagedObjectSource<?, ?> managedObjectSource,
			String sectionLocation, CompilerIssues issues) {
		this.managedObjectName = managedObjectName;
		this.managedObjectSourceClassName = managedObjectSourceClassName;
		this.managedObjectSource = managedObjectSource;
		this.sectionLocation = sectionLocation;
		this.issues = issues;
	}

	/*
	 * ==================== SectionManagedObject ===============================
	 */

	@Override
	public String getSectionManagedObjectName() {
		return this.managedObjectName;
	}

	@Override
	public void addProperty(String name, String value) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement SectionManagedObject.addProperty");
	}

	@Override
	public ManagedObjectDependency getManagedObjectDependency(
			String managedObjectDependencyName) {
		// Obtain and return the dependency for the name
		ManagedObjectDependencyNode dependency = this.depedencies
				.get(managedObjectDependencyName);
		if (dependency == null) {
			// Add the managed object dependency
			dependency = new ManagedObjectDependencyNodeImpl(
					managedObjectDependencyName, this.sectionLocation,
					this.issues);
			this.depedencies.put(managedObjectDependencyName, dependency);
		}
		return dependency;
	}

	@Override
	public ManagedObjectFlow getManagedObjectFlow(String managedObjectFlowName) {
		// Obtain and return the flow for the name
		ManagedObjectFlowNode flow = this.flows.get(managedObjectFlowName);
		if (flow == null) {
			// Add the managed object flow
			flow = new ManagedObjectFlowNodeImpl(managedObjectFlowName,
					this.sectionLocation, this.issues);
			this.flows.put(managedObjectFlowName, flow);
		}
		return flow;
	}

	/*
	 * =================== LinkObjectNode ======================================
	 */

	/**
	 * Linked {@link LinkObjectNode}.
	 */
	private LinkObjectNode linkedObjectName;

	@Override
	public boolean linkObjectNode(LinkObjectNode node) {
		// Link
		this.linkedObjectName = node;
		return true;
	}

	@Override
	public LinkObjectNode getLinkedObjectNode() {
		return this.linkedObjectName;
	}

}