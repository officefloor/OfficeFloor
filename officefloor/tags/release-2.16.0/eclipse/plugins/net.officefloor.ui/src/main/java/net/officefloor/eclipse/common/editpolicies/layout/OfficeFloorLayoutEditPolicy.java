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
package net.officefloor.eclipse.common.editpolicies.layout;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.eclipse.common.commands.ChangeCommand;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorDiagramEditPart;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.model.Model;
import net.officefloor.model.change.Change;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.editpolicies.LayoutEditPolicy;
import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.CreateRequest;

/**
 * {@link LayoutEditPolicy} for the {@link AbstractOfficeFloorDiagramEditPart}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorLayoutEditPolicy extends XYLayoutEditPolicy {

	/**
	 * {@link CreateChangeFactory} instances by the {@link Object} they can
	 * create.
	 */
	private final Map<Class<?>, CreateChangeFactory<?>> createFactories = new HashMap<Class<?>, CreateChangeFactory<?>>();

	/**
	 * {@link ChildEditPolicyFactory} instances by the child {@link Model} type.
	 */
	private final Map<Class<?>, ChildEditPolicyFactory<?>> childFactories = new HashMap<Class<?>, ChildEditPolicyFactory<?>>();

	/**
	 * Default {@link ChildEditPolicyFactory}.
	 */
	@SuppressWarnings("rawtypes")
	private ChildEditPolicyFactory defaultChildEditPolicyFactory = new ChildEditPolicyFactory<Object>() {
		@Override
		public EditPolicy createEditPolicy(Object target) {
			return new NonResizableEditPolicy();
		}
	};

	/**
	 * {@link ConstraintChangeFactory} instances by the {@link Model} they
	 * change constraints on.
	 */
	private final Map<Class<?>, ConstraintChangeFactory<?>> constraintFactories = new HashMap<Class<?>, ConstraintChangeFactory<?>>();

	/**
	 * {@link DeleteChangeFactory} instances by the {@link Model} they delete.
	 */
	private final Map<Class<?>, DeleteChangeFactory<?>> deleteFactories = new HashMap<Class<?>, DeleteChangeFactory<?>>();

	/**
	 * Registers a {@link CreateChangeFactory}.
	 * 
	 * @param <O>
	 *            {@link Object} type.
	 * @param objectType
	 *            Type of new {@link Object} to be added.
	 * @param factory
	 *            {@link CreateChangeFactory} to add the new {@link Object} of
	 *            the type.
	 */
	public <O> void addCreate(Class<O> objectType,
			CreateChangeFactory<O> factory) {
		this.createFactories.put(objectType, factory);
	}

	/**
	 * Registers a {@link ChildEditPolicyFactory}.
	 * 
	 * @param <M>
	 *            {@link Model} type.
	 * @param modelType
	 *            Type of child {@link Model}.
	 * @param factory
	 *            {@link ChildEditPolicyFactory} to create {@link EditPolicy}
	 *            for child {@link Model}.
	 */
	public <M> void addChild(Class<M> modelType,
			ChildEditPolicyFactory<M> factory) {
		this.childFactories.put(modelType, factory);
	}

	/**
	 * Specifies the default {@link ChildEditPolicyFactory}.
	 * 
	 * @param factory
	 *            {@link ChildEditPolicyFactory} to use should there be no
	 *            specific {@link ChildEditPolicyFactory}.
	 */
	public void setDefaultChild(ChildEditPolicyFactory<?> factory) {
		this.defaultChildEditPolicyFactory = factory;
	}

	/**
	 * Registers a {@link ConstraintChangeFactory}.
	 * 
	 * @param <M>
	 *            {@link Model} type.
	 * @param modelType
	 *            Type of {@link Model} to have its constraints changed.
	 * @param factory
	 *            {@link ConstraintChangeFactory} to change constraints on the
	 *            {@link Model} instances of the type.
	 */
	public <M> void addConstraint(Class<M> modelType,
			ConstraintChangeFactory<M> factory) {
		this.constraintFactories.put(modelType, factory);
	}

	/**
	 * Registers a {@link DeleteChangeFactory}.
	 * 
	 * @param <M>
	 *            {@link Model} type.
	 * @param modelType
	 *            Type of {@link Model} to be deleted.
	 * @param factory
	 *            {@link DeleteChangeFactory} to delete the {@link Model}
	 *            instances of the type.
	 */
	public <M> void addDelete(Class<M> modelType, DeleteChangeFactory<M> factory) {
		this.deleteFactories.put(modelType, factory);
	}

	/*
	 * =================== LayoutEditPolicy ===================================
	 */

	@Override
	@SuppressWarnings("rawtypes")
	protected Command getCreateCommand(CreateRequest request) {

		// Obtain the details of the create
		Object newObject = request.getNewObject();
		Point location = request.getLocation();

		// Obtain the create factory
		Class<?> objectType = newObject.getClass();
		CreateChangeFactory factory = this.createFactories.get(objectType);
		if (factory == null) {
			return null; // must have factory
		}

		// Create the change
		Change<?> change = factory.createChange(newObject, location);
		if (change == null) {
			return null; // no change
		}

		// Return the change
		return new ChangeCommand(change);
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected EditPolicy createChildEditPolicy(EditPart child) {

		// Obtain the model
		Object model = child.getModel();

		// Determine if specific child edit policy
		Class<?> modelType = model.getClass();
		ChildEditPolicyFactory factory = this.childFactories.get(modelType);
		if (factory != null) {
			// Return specific child edit policy
			return factory.createEditPolicy(model);
		}

		// Use the default child edit policy
		return this.defaultChildEditPolicyFactory.createEditPolicy(model);
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Command createChangeConstraintCommand(EditPart child,
			Object constraint) {

		// Obtain the bounds of the constraint
		Rectangle rectangle = (Rectangle) constraint;

		// Obtain the edit part and its positional model
		AbstractOfficeFloorEditPart<?, ?, ?> editPart = (AbstractOfficeFloorEditPart<?, ?, ?>) child;

		// Obtain the constraint factory
		Object model = child.getModel();
		Class<?> modelType = model.getClass();
		ConstraintChangeFactory factory = this.constraintFactories
				.get(modelType);
		if (factory != null) {
			// Let factory determine if can change constraint
			Change change = factory.createChange(model, rectangle);
			if (change == null) {
				return null; // no change on constraint
			}

			// Return change on constraint
			return new ChangeCommand(change);
		}

		// Return the move command by default
		return new MovePositionalModelCommand(editPart, rectangle.getLocation());
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Command getDeleteDependantCommand(Request request) {

		// Obtain the model to delete
		if (!(request instanceof DeleteRequest)) {
			return null; // Unknown request
		}
		DeleteRequest deleteRequest = (DeleteRequest) request;

		// Populate a compound command to delete all edit parts
		EditPart[] editParts = deleteRequest.getEditPartsToDelete();
		CompoundCommand deleteCommand = new CompoundCommand("Delete");
		for (EditPart editPart : editParts) {
			Object model = editPart.getModel();

			// Determine if have factory to delete
			Class<?> modelType = model.getClass();
			DeleteChangeFactory factory = this.deleteFactories.get(modelType);
			if (factory == null) {
				// Not able to delete the model as no factory
				continue;
			}

			// Create the change to delete the model
			Change<?> change = factory.createChange(model);
			if (change == null) {
				// No change so can not delete model
				continue;
			}

			// Add command to delete the model
			deleteCommand.add(new ChangeCommand(change));
		}

		// Return delete command if contains deletions
		if (deleteCommand.getCommands().size() > 0) {
			// May delete (provide simplest command for deleting)
			return deleteCommand.unwrap();
		} else {
			// No deletes so can not delete
			return null;
		}
	}

}