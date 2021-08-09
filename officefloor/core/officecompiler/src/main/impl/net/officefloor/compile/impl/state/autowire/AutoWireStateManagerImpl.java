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

package net.officefloor.compile.impl.state.autowire;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.internal.structure.AutoWire;
import net.officefloor.compile.internal.structure.AutoWireLink;
import net.officefloor.compile.internal.structure.AutoWirer;
import net.officefloor.compile.internal.structure.BoundManagedObjectNode;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.spi.supplier.source.InternalSupplier;
import net.officefloor.compile.spi.supplier.source.SuppliedManagedObjectSource;
import net.officefloor.compile.state.autowire.AutoWireStateManager;
import net.officefloor.frame.api.manage.ObjectUser;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.StateManager;
import net.officefloor.frame.api.manage.UnknownObjectException;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.impl.execute.office.ObjectUserImpl;
import net.officefloor.frame.internal.structure.MonitorClock;

/**
 * {@link AutoWireStateManager} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireStateManagerImpl implements AutoWireStateManager, Node {

	/**
	 * {@link Office}.
	 */
	private final Office office;

	/**
	 * {@link StateManager}.
	 */
	private final StateManager stateManager;

	/**
	 * {@link OfficeNode}.
	 */
	private final OfficeNode officeNode;

	/**
	 * {@link AutoWirer}.
	 */
	private final AutoWirer<LinkObjectNode> autoWirer;

	/**
	 * {@link InternalSupplier} instances.
	 */
	private final InternalSupplier[] internalSuppliers;

	/**
	 * {@link MonitorClock}.
	 */
	private final MonitorClock monitorClock;

	/**
	 * Instantiate.
	 * 
	 * @param office            {@link Office}.
	 * @param stateManager      {@link StateManager}.
	 * @param officeNode        {@link OfficeNode}.
	 * @param autoWirer         {@link AutoWirer}.
	 * @param internalSuppliers {@link InternalSupplier} instances.
	 * @param monitorClock      {@link MonitorClock}.
	 */
	public AutoWireStateManagerImpl(Office office, StateManager stateManager, OfficeNode officeNode,
			AutoWirer<LinkObjectNode> autoWirer, InternalSupplier[] internalSuppliers, MonitorClock monitorClock) {
		this.office = office;
		this.stateManager = stateManager;
		this.officeNode = officeNode;
		this.autoWirer = autoWirer;
		this.internalSuppliers = internalSuppliers;
		this.monitorClock = monitorClock;
	}

	/**
	 * Obtains the {@link AutoWireObjectReference}.
	 * 
	 * @param qualifier  Qualifier for the {@link ManagedObject}. May be
	 *                   <code>null</code>.
	 * @param objectType Object type of the {@link ManagedObject}.
	 * @return {@link AutoWireObjectReference}.
	 */
	private AutoWireObjectReference getAutoWireObjectReference(String qualifier, Class<?> objectType) {

		// Obtain the auto wire link
		AutoWireLink<?, LinkObjectNode>[] links = this.autoWirer.findAutoWireLinks(this,
				new AutoWire(qualifier, objectType));
		if (links.length != 1) {
			return new AutoWireObjectReference(null, links.length > 1,
					this.getQualifiedName(qualifier, objectType) + " has " + links.length + " auto wire matches");
		}

		// Obtain the bound node
		LinkObjectNode objectNode = links[0].getTargetNode(this.officeNode);
		BoundManagedObjectNode boundObjectNode = (objectNode instanceof BoundManagedObjectNode)
				? (BoundManagedObjectNode) objectNode
				: LinkUtil.retrieveTarget(objectNode, BoundManagedObjectNode.class, null);

		// Ensure object actually bound (supplied managed objects may not be used)
		String boundObjectName = boundObjectNode.getBoundManagedObjectName();
		for (String objectName : this.office.getObjectNames()) {
			if (boundObjectName.equals(objectName)) {
				return new AutoWireObjectReference(boundObjectName, false, null); // object available
			}
		}

		// As here, the bound object is not available
		return new AutoWireObjectReference(null, false, this.getQualifiedName(qualifier, objectType) + " is not used "
				+ SuppliedManagedObjectSource.class.getSimpleName());
	}

	/**
	 * Obtains the matching {@link InternalSupplier} instances.
	 * 
	 * @param qualifier  Qualifier. May be <code>null</code>.
	 * @param objectType Required object type.
	 * @return {@link InternalSupplier} instances that provide the object.
	 */
	private List<InternalSupplier> getMatchingInternalSuppliers(String qualifier, Class<?> objectType) {

		// Find the matching internal suppliers
		List<InternalSupplier> matchingInternalSuppliers = new LinkedList<>();
		for (InternalSupplier internalSupplier : this.internalSuppliers) {
			if (internalSupplier.isObjectAvailable(qualifier, objectType)) {
				matchingInternalSuppliers.add(internalSupplier);
			}
		}

		// Return the matching internal suppliers
		return matchingInternalSuppliers;
	}

	/**
	 * Obtains the qualified name.
	 * 
	 * @param qualifier  Qualifier. May be <code>null</code>.
	 * @param objectType Required object type.
	 * @return Qualified name.
	 */
	private String getQualifiedName(String qualifier, Class<?> objectType) {
		return (qualifier != null ? qualifier + ":" : "") + objectType.getName();
	}

	/*
	 * ================== AutoWireStateManager ====================
	 */

	@Override
	public boolean isObjectAvailable(String qualifier, Class<?> objectType) {

		// Check OfficeFloor to see if available
		AutoWireObjectReference objectReference = this.getAutoWireObjectReference(qualifier, objectType);
		if (objectReference.boundObjectName != null) {
			return true; // available from OfficeFloor
		}

		// Available only if single instance supplier
		List<InternalSupplier> matchingInternalSuppliers = this.getMatchingInternalSuppliers(qualifier, objectType);
		return matchingInternalSuppliers.size() == 1;
	}

	@Override
	public <O> void load(String qualifier, Class<? extends O> objectType, ObjectUser<O> user)
			throws UnknownObjectException {

		// Check OfficeFloor to see if available
		AutoWireObjectReference objectReference = this.getAutoWireObjectReference(qualifier, objectType);
		if (objectReference.boundObjectName != null) {

			// Load the object from OfficeFloor
			this.stateManager.load(objectReference.boundObjectName, user);
			return;
		}

		// Determine if load from internal supplier
		List<InternalSupplier> matchingInternalSuppliers = this.getMatchingInternalSuppliers(qualifier, objectType);
		switch (matchingInternalSuppliers.size()) {
		case 0:
			// Propagate OfficeFloor no match
			throw new UnknownObjectException(objectReference.errorMessage);

		case 1:
			// Return internally supplied
			matchingInternalSuppliers.get(0).load(qualifier, objectType, user);
			break;

		default:
			// More than one match, so provide priority error
			throw new UnknownObjectException(objectReference.isPriorityError ? objectReference.errorMessage
					: this.getQualifiedName() + " is available from " + matchingInternalSuppliers.size() + " "
							+ InternalSupplier.class.getSimpleName() + " instances");
		}
	}

	@Override
	public <O> O getObject(String qualifier, Class<? extends O> objectType, long timeoutInMilliseconds)
			throws UnknownObjectException, Throwable {

		// Check OfficeFloor to see if available
		AutoWireObjectReference objectReference = this.getAutoWireObjectReference(qualifier, objectType);
		if (objectReference.boundObjectName != null) {

			// Load the object from OfficeFloor
			return this.stateManager.getObject(objectReference.boundObjectName, timeoutInMilliseconds);
		}

		// Determine if load from internal supplier
		List<InternalSupplier> matchingInternalSuppliers = this.getMatchingInternalSuppliers(qualifier, objectType);
		switch (matchingInternalSuppliers.size()) {
		case 0:
			// Propagate OfficeFloor no match
			throw new UnknownObjectException(objectReference.errorMessage);

		case 1:
			// Return internally supplied
			ObjectUserImpl<O> user = new ObjectUserImpl<>(this.getQualifiedName(qualifier, objectType),
					this.monitorClock);
			matchingInternalSuppliers.get(0).load(qualifier, objectType, user);
			return user.getObject(timeoutInMilliseconds);

		default:
			// More than one match, so provide priority error
			throw new UnknownObjectException(objectReference.isPriorityError ? objectReference.errorMessage
					: this.getQualifiedName() + " is available from " + matchingInternalSuppliers.size() + " "
							+ InternalSupplier.class.getSimpleName() + " instances");
		}
	}

	@Override
	public void close() throws Exception {
		this.stateManager.close();
	}

	/*
	 * ========================= Node ====================================
	 */

	@Override
	public String getNodeName() {
		return AutoWireStateManager.class.getSimpleName();
	}

	@Override
	public String getNodeType() {
		return this.getNodeName();
	}

	@Override
	public String getLocation() {
		return null;
	}

	@Override
	public Node getParentNode() {
		return null;
	}

	@Override
	public boolean isInitialised() {
		return true;
	}

	@Override
	public Node[] getChildNodes() {
		return new Node[0];
	}

	/**
	 * Auto-wire object reference.
	 */
	private static class AutoWireObjectReference {

		/**
		 * Bound object name. Will be <code>null</code> if error.
		 */
		private final String boundObjectName;

		/**
		 * Indicates if priority error.
		 */
		private final boolean isPriorityError;

		/**
		 * Error message if can not determine bound object name.
		 */
		private final String errorMessage;

		/**
		 * Instantiate.
		 * 
		 * @param boundObjectName Bound object name. Will be <code>null</code> if error.
		 * @param isPriorityError Indicates if priority error.
		 * @param errorMessage    Error message if can not determine bound object name.
		 */
		private AutoWireObjectReference(String boundObjectName, boolean isPriorityError, String errorMessage) {
			this.boundObjectName = boundObjectName;
			this.isPriorityError = isPriorityError;
			this.errorMessage = errorMessage;
		}
	}

}
