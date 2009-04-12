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

import net.officefloor.compile.impl.managedobject.ManagedObjectLoaderImpl;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectDependencyNode;
import net.officefloor.compile.internal.structure.ManagedObjectFlowNode;
import net.officefloor.compile.internal.structure.ManagedObjectNode;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.model.repository.ConfigurationContext;

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
	 * {@link PropertyList} to load the {@link ManagedObjectSource}.
	 */
	private final PropertyList propertyList = new PropertyListImpl();

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
	 * Loaded {@link ManagedObjectType}.
	 */
	private ManagedObjectType<?> managedObjectType;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectName
	 *            Name of this {@link SectionManagedObject}.
	 * @param managedObjectSourceClassName
	 *            Class name of the {@link ManagedObjectSource}.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection} containing this
	 *            {@link SectionManagedObject}.
	 * @param issues
	 *            {@link CompilerIssues}.
	 */
	public ManagedObjectNodeImpl(String managedObjectName,
			String managedObjectSourceClassName, String sectionLocation,
			CompilerIssues issues) {
		this.managedObjectName = managedObjectName;
		this.managedObjectSourceClassName = managedObjectSourceClassName;
		this.sectionLocation = sectionLocation;
		this.issues = issues;
	}

	/*
	 * ===================== ManagedObjectNode ================================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public void loadManagedObjectMetaData(
			ConfigurationContext configurationContext, ClassLoader classLoader) {

		// Obtain the managed object source class
		Class<? extends ManagedObjectSource> managedObjectSourceClass = CompileUtil
				.obtainClass(this.managedObjectSourceClassName,
						ManagedObjectSource.class, classLoader,
						LocationType.SECTION, this.sectionLocation,
						AssetType.MANAGED_OBJECT, this.managedObjectName,
						this.issues);
		if (managedObjectSourceClass == null) {
			return; // must have managed object source class
		}

		// Create the loader to obtain the managed object type
		ManagedObjectLoader loader = new ManagedObjectLoaderImpl(
				LocationType.SECTION, this.sectionLocation,
				this.managedObjectName);

		// Load the managed object type
		this.managedObjectType = loader.loadManagedObject(
				managedObjectSourceClass, this.propertyList, classLoader,
				this.issues);
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
		this.propertyList.addProperty(name).setValue(value);
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
	 * ================ OfficeSectionManagedObject ============================
	 */

	@Override
	public String getOfficeSectionManagedObjectName() {
		return this.managedObjectName;
	}

	@Override
	public Class<?>[] getSupportedExtensionInterfaces() {

		// Ensure this not is initialised
		if (this.managedObjectType == null) {
			throw new IllegalStateException(
					"Should not obtain supported extension interfaces before being initialised");
		}

		// Return the supported extension interfaces
		return this.managedObjectType.getExtensionInterfaces();
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