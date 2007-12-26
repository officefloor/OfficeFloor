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
package net.officefloor.eclipse.office.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorSourceNodeEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.common.editparts.RemovableEditPart;
import net.officefloor.eclipse.common.editpolicies.ConnectionModelFactory;
import net.officefloor.eclipse.common.figure.FreeformWrapperFigure;
import net.officefloor.eclipse.common.figure.IndentFigure;
import net.officefloor.eclipse.common.figure.ListFigure;
import net.officefloor.eclipse.common.figure.ListItemFigure;
import net.officefloor.eclipse.common.figure.WrappingFigure;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.RemoveConnectionsAction;
import net.officefloor.model.office.AdministratorModel;
import net.officefloor.model.office.AdministratorToManagedObjectModel;
import net.officefloor.model.office.ExternalManagedObjectModel;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.office.AdministratorModel.AdministratorEvent;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.requests.CreateConnectionRequest;

/**
 * {@link org.eclipse.gef.EditPart} for the
 * {@link net.officefloor.model.office.AdministratorModel}.
 * 
 * @author Daniel
 */
public class AdministratorEditPart extends
		AbstractOfficeFloorSourceNodeEditPart<AdministratorModel> implements
		RemovableEditPart {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#populatePropertyChangeHandlers(java.util.List)
	 */
	@Override
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler<?>> handlers) {
		handlers.add(new PropertyChangeHandler<AdministratorEvent>(
				AdministratorEvent.values()) {
			@Override
			protected void handlePropertyChange(AdministratorEvent property,
					PropertyChangeEvent evt) {
				switch (property) {
				case ADD_DUTY:
				case REMOVE_DUTY:
					AdministratorEditPart.this.refreshChildren();
					break;
				case ADD_MANAGED_OBJECT:
				case REMOVE_MANAGED_OBJECT:
					AdministratorEditPart.this.refreshSourceConnections();
					break;
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	@Override
	protected IFigure createFigure() {

		// Create the indent figure to contain children
		IndentFigure indentFigure = new IndentFigure(10, new ListFigure());
		indentFigure.setBackgroundColor(ColorConstants.cyan);

		// Create the figure
		WrappingFigure figure = new WrappingFigure(indentFigure);
		figure.addDecorate(new ListItemFigure(this.getCastedModel().getId()));
		figure.addChildContainerFigure();
		figure.setBackgroundColor(ColorConstants.cyan);

		// Return the figure for free form display
		FreeformWrapperFigure wrappingFigure = new FreeformWrapperFigure(figure);
		wrappingFigure.setBackgroundColor(ColorConstants.cyan);
		return wrappingFigure;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#populateModelChildren(java.util.List)
	 */
	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel().getDuties());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorSourceNodeEditPart#createConnectionModelFactory()
	 */
	@Override
	protected ConnectionModelFactory createConnectionModelFactory() {
		return new ConnectionModelFactory() {
			public ConnectionModel createConnection(Object source,
					Object target, CreateConnectionRequest request) {

				// Create the connection
				AdministratorToManagedObjectModel conn = new AdministratorToManagedObjectModel();
				conn.setAdministrator((AdministratorModel) source);
				conn.setManagedObject((ExternalManagedObjectModel) target);
				conn.connect();

				// Return the connection
				return conn;
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorSourceNodeEditPart#populateConnectionTargetTypes(java.util.List)
	 */
	@Override
	protected void populateConnectionTargetTypes(List<Class<?>> types) {
		types.add(ExternalManagedObjectModel.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart#populateConnectionSourceModels(java.util.List)
	 */
	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		models.addAll(this.getCastedModel().getManagedObjects());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart#populateConnectionTargetModels(java.util.List)
	 */
	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		// Never a target
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.RemovableEditPart#delete()
	 */
	@Override
	public void delete() {
		// Disconnect and remove Administrator
		RemoveConnectionsAction<AdministratorModel> administrator = this
				.getCastedModel().removeConnections();
		OfficeModel office = (OfficeModel) this.getParent().getModel();
		office.removeAdministrator(administrator.getModel());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.RemovableEditPart#undelete()
	 */
	@Override
	public void undelete() {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement AdministratorEditPart.undelete");
	}

}
