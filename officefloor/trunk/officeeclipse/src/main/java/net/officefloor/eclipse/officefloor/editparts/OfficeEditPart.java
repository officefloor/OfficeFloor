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
package net.officefloor.eclipse.officefloor.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.common.figure.FreeformWrapperFigure;
import net.officefloor.eclipse.officefloor.figure.OfficeFigure;
import net.officefloor.model.officefloor.OfficeFloorOfficeModel;
import net.officefloor.model.officefloor.OfficeFloorOfficeModel.OfficeFloorOfficeEvent;

import org.eclipse.draw2d.IFigure;

/**
 * {@link org.eclipse.gef.EditPart} for the
 * {@link net.officefloor.model.officefloor.OfficeFloorOfficeModel}.
 * 
 * @author Daniel
 */
public class OfficeEditPart extends
		AbstractOfficeFloorNodeEditPart<OfficeFloorOfficeModel> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#populatePropertyChangeHandlers(java.util.List)
	 */
	@Override
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler> handlers) {
		handlers.add(new PropertyChangeHandler<OfficeFloorOfficeEvent>(
				OfficeFloorOfficeEvent.values()) {
			@Override
			protected void handlePropertyChange(
					OfficeFloorOfficeEvent property, PropertyChangeEvent evt) {
				switch (property) {
				case ADD_MANAGED_OBJECT:
				case REMOVE_MANAGED_OBJECT:
				case ADD_TEAM:
				case REMOVE_TEAM:
					OfficeEditPart.this.refreshChildren();
					break;
				case ADD_RESPONSIBLE_MANAGED_OBJECT:
				case REMOVE_RESPONSIBLE_MANAGED_OBJECT:
					OfficeEditPart.this.refreshTargetConnections();
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
		return new FreeformWrapperFigure(new OfficeFigure(this.getCastedModel()
				.getName()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#populateModelChildren(java.util.List)
	 */
	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel().getTeams());
		childModels.addAll(this.getCastedModel().getManagedObjects());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart#populateConnectionSourceModels(java.util.List)
	 */
	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		// Never a source
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart#populateConnectionTargetModels(java.util.List)
	 */
	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		models.addAll(this.getCastedModel().getResponsibleManagedObjects());
	}

}
