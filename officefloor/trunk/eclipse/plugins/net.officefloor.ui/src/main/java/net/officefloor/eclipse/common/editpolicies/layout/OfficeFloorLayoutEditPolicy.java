/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.editpolicies.LayoutEditPolicy;
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
	 * {@link DeleteChangeFactory} instances by the {@link Model} they delete.
	 */
	private final Map<Class<?>, DeleteChangeFactory<?>> deleteFactories = new HashMap<Class<?>, DeleteChangeFactory<?>>();

	/**
	 * Registers a {@link CreateChangeFactory}.
	 * 
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
	 * Registers a {@link DeleteChangeFactory}.
	 * 
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
	protected Command createChangeConstraintCommand(EditPart child,
			Object constraint) {

		// Obtain the bounds of the constraint
		Rectangle rectangle = (Rectangle) constraint;

		// Obtain the edit part and its positional model
		AbstractOfficeFloorEditPart<?, ?, ?> editPart = (AbstractOfficeFloorEditPart<?, ?, ?>) child;

		// Return the move command
		return new MovePositionalModelCommand(editPart, rectangle.getLocation());
	}

	@Override
	@SuppressWarnings("unchecked")
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
	@SuppressWarnings("unchecked")
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