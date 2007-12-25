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
package net.officefloor.eclipse.desk.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.common.editparts.RemovableEditPart;
import net.officefloor.model.RemoveConnectionsAction;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.ExternalManagedObjectModel;
import net.officefloor.model.desk.ExternalManagedObjectModel.ExternalManagedObjectEvent;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;

/**
 * {@link org.eclipse.gef.EditPart} for the
 * {@link net.officefloor.model.desk.ExternalManagedObjectModel}.
 * 
 * @author Daniel
 */
public class ExternalManagedObjectEditPart extends
		AbstractOfficeFloorNodeEditPart<ExternalManagedObjectModel> implements
		RemovableEditPart {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	protected IFigure createFigure() {
		Label figure = new Label(this.getCastedModel().getName());
		figure.setBackgroundColor(ColorConstants.lightGray);
		figure.setOpaque(true);
		figure.setLayoutManager(new FlowLayout(true));

		// Return figure
		return figure;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart#populateConnectionSourceModels(java.util.List)
	 */
	protected void populateConnectionSourceModels(List<Object> models) {
		// Not a source
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart#populateConnectionTargetModels(java.util.List)
	 */
	protected void populateConnectionTargetModels(List<Object> models) {
		models.addAll(this.getCastedModel().getTaskObjects());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#populatePropertyChangeHandlers(java.util.List)
	 */
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler> handlers) {
		handlers.add(new PropertyChangeHandler<ExternalManagedObjectEvent>(
				ExternalManagedObjectEvent.values()) {
			protected void handlePropertyChange(
					ExternalManagedObjectEvent property, PropertyChangeEvent evt) {
				switch (property) {
				case ADD_TASK_OBJECT:
				case REMOVE_TASK_OBJECT:
					ExternalManagedObjectEditPart.this
							.refreshTargetConnections();
					break;
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.RemovableEditPart#delete()
	 */
	@Override
	public void delete() {
		// Disconnect and remove the Managed Object
		RemoveConnectionsAction<ExternalManagedObjectModel> mo = this.getCastedModel().removeConnections();
		DeskModel desk = (DeskModel) this.getParent().getParent().getModel();
		desk.removeExternalManagedObject(mo.getModel());
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
				"TODO implement ExternalManagedObjectEditPart.undelete");
	}

}
