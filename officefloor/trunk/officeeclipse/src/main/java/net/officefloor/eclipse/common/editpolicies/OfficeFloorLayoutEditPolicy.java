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
package net.officefloor.eclipse.common.editpolicies;

import net.officefloor.eclipse.common.commands.CreateCommand;
import net.officefloor.eclipse.common.commands.DeleteEditPartCommand;
import net.officefloor.eclipse.common.commands.MovePositionalModelCommand;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.common.editparts.RemovableEditPart;
import net.officefloor.eclipse.common.requests.DeleteRequest;
import net.officefloor.model.AbstractModel;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.CreateRequest;

/**
 * {@link org.eclipse.gef.editpolicies.LayoutEditPolicy} for the Office Floor
 * Editors.
 * 
 * @author Daniel
 */
public abstract class OfficeFloorLayoutEditPolicy<P> extends XYLayoutEditPolicy {

	/**
	 * Parent model of the new model being added.
	 */
	protected P parentModel;

	/**
	 * Convenience initiator so that sub classes do not require providing the
	 * constructor.
	 * 
	 * @param parentModel
	 *            Parent model.
	 * @return this.
	 */
	public OfficeFloorLayoutEditPolicy<P> init(P parentModel) {
		// Store state
		this.parentModel = parentModel;

		// Return this
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editpolicies.ConstrainedLayoutEditPolicy#createAddCommand(org.eclipse.gef.EditPart,
	 *      java.lang.Object)
	 */
	protected Command createAddCommand(EditPart child, Object constraint) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editpolicies.ConstrainedLayoutEditPolicy#createChangeConstraintCommand(org.eclipse.gef.EditPart,
	 *      java.lang.Object)
	 */
	protected Command createChangeConstraintCommand(EditPart child,
			Object constraint) {

		// Obtain the bounds of the constraint
		Rectangle rectangle = (Rectangle) constraint;

		// Obtain the edit part and its positional model
		AbstractOfficeFloorEditPart<?> editPart = (AbstractOfficeFloorEditPart<?>) child;

		// Return the move command
		return new MovePositionalModelCommand(editPart, rectangle.getLocation());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editpolicies.LayoutEditPolicy#getCreateCommand(org.eclipse.gef.requests.CreateRequest)
	 */
	protected Command getCreateCommand(CreateRequest request) {
		// Return the create command
		return this.createCreateComand(this.parentModel,
				request.getNewObject(), request.getLocation());
	}

	/**
	 * Creates the {@link Command} to create the new model.
	 * 
	 * @param parentModel
	 *            Parent model for the new model.
	 * @param newModel
	 *            The new model to be added.
	 * @param location
	 *            Location to create the new model.
	 * @return Command to create the new model within the model.
	 * @see #loadLocation(Object, Point)
	 */
	protected abstract CreateCommand<?, ?> createCreateComand(P parentModel,
			Object newModel, Point location);

	/**
	 * <p>
	 * Convenience method to load the location on the new model.
	 * </p>
	 * <p>
	 * For the location to be loaded to the model it must extend
	 * {@link AbstractPositionedModel}.
	 * </p>
	 * 
	 * @param newModel
	 *            New model that should implement
	 *            {@link AbstractPositionedModel}.
	 * @param location
	 *            Location to specify on the model.
	 */
	protected void loadLocation(Object newModel, Point location) {
		if (newModel instanceof AbstractModel) {
			AbstractModel positionedModel = (AbstractModel) newModel;

			// Specify location on model
			positionedModel.setX(location.x);
			positionedModel.setY(location.y);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editpolicies.LayoutEditPolicy#getDeleteDependantCommand(org.eclipse.gef.Request)
	 */
	protected Command getDeleteDependantCommand(Request request) {

		// Obtain the edit part to delete
		DeleteRequest deleteRequest = (DeleteRequest) request;
		EditPart editPart = deleteRequest.getSender();

		// Create delete command if edit part removable
		if (editPart instanceof RemovableEditPart) {
			return new DeleteEditPartCommand((RemovableEditPart) editPart);
		} else {
			// Can not delete
			return null;
		}
	}

}