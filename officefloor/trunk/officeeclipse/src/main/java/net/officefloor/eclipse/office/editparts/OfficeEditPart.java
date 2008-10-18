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

import net.officefloor.eclipse.common.commands.CreateCommand;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorDiagramEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.common.editpolicies.OfficeFloorLayoutEditPolicy;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.office.OfficeRoomModel;
import net.officefloor.model.office.OfficeModel.OfficeEvent;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link OfficeModel}.
 * 
 * @author Daniel
 */
public class OfficeEditPart extends
		AbstractOfficeFloorDiagramEditPart<OfficeModel> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorDiagramEditPart
	 * #createLayoutEditPolicy()
	 */
	@Override
	protected OfficeFloorLayoutEditPolicy<?> createLayoutEditPolicy() {
		return new OfficeLayoutEditPolicy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorDiagramEditPart
	 * #populateChildren(java.util.List)
	 */
	@Override
	protected void populateChildren(List<Object> childModels) {
		OfficeModel office = this.getCastedModel();
		OfficeRoomModel room = office.getRoom();
		if (room != null) {
			childModels.add(office.getRoom());
		}
		childModels.addAll(office.getExternalTeams());
		childModels.addAll(office.getExternalManagedObjects());
		childModels.addAll(office.getAdministrators());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#
	 * populatePropertyChangeHandlers(java.util.List)
	 */
	@Override
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler<?>> handlers) {
		handlers.add(new PropertyChangeHandler<OfficeEvent>(OfficeEvent
				.values()) {
			@Override
			protected void handlePropertyChange(OfficeEvent property,
					PropertyChangeEvent evt) {
				switch (property) {
				case ADD_EXTERNAL_TEAM:
				case REMOVE_EXTERNAL_TEAM:
				case ADD_ADMINISTRATOR:
				case REMOVE_ADMINISTRATOR:
				case ADD_EXTERNAL_MANAGED_OBJECT:
				case REMOVE_EXTERNAL_MANAGED_OBJECT:
				case CHANGE_ROOM:
					OfficeEditPart.this.refreshChildren();
					break;
				}
			}
		});
	}
}

/**
 * {@link org.eclipse.gef.editpolicies.LayoutEditPolicy} for the
 * {@link net.officefloor.model.office.OfficeModel}.
 */
class OfficeLayoutEditPolicy extends OfficeFloorLayoutEditPolicy<OfficeModel> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editpolicies.OfficeFloorLayoutEditPolicy
	 * #createCreateComand(P, java.lang.Object,
	 * org.eclipse.draw2d.geometry.Point)
	 */
	@Override
	protected CreateCommand<?, ?> createCreateComand(OfficeModel parentModel,
			Object newModel, Point location) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO implement");
	}

}
