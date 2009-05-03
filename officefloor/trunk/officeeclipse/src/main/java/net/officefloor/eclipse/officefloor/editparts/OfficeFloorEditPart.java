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

import net.officefloor.eclipse.common.commands.CreateCommand;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorDiagramEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.common.editpolicies.OfficeFloorLayoutEditPolicy;
import net.officefloor.model.officefloor.OfficeFloorModel;
import net.officefloor.model.officefloor.OfficeFloorModel.OfficeFloorEvent;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.editpolicies.LayoutEditPolicy;

/**
 * {@link EditPart} for the {@link OfficeFloorModel}.
 * 
 * @author Daniel
 */
public class OfficeFloorEditPart extends
		AbstractOfficeFloorDiagramEditPart<OfficeFloorModel> {

	@Override
	protected OfficeFloorLayoutEditPolicy<?> createLayoutEditPolicy() {
		return new OfficeFloorOfficeFloorLayoutEditPolicy();
	}

	@Override
	protected void populateChildren(List<Object> childModels) {
		OfficeFloorModel officeFloor = this.getCastedModel();
		childModels.addAll(officeFloor.getDeployedOffices());
		childModels.addAll(officeFloor.getOfficeFloorTeams());
		childModels.addAll(officeFloor.getOfficeFloorManagedObjectSources());
		childModels.addAll(officeFloor.getOfficeFloorManagedObjects());
	}

	@Override
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler<?>> handlers) {
		handlers.add(new PropertyChangeHandler<OfficeFloorEvent>(
				OfficeFloorEvent.values()) {
			@Override
			protected void handlePropertyChange(OfficeFloorEvent property,
					PropertyChangeEvent evt) {
				switch (property) {
				case ADD_DEPLOYED_OFFICE:
				case REMOVE_DEPLOYED_OFFICE:
				case ADD_OFFICE_FLOOR_TEAM:
				case REMOVE_OFFICE_FLOOR_TEAM:
				case ADD_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE:
				case REMOVE_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE:
				case ADD_OFFICE_FLOOR_MANAGED_OBJECT:
				case REMOVE_OFFICE_FLOOR_MANAGED_OBJECT:
					OfficeFloorEditPart.this.refreshChildren();
					break;
				}
			}
		});
	}

}

/**
 * {@link LayoutEditPolicy} for the {@link OfficeFloorModel}.
 */
class OfficeFloorOfficeFloorLayoutEditPolicy extends
		OfficeFloorLayoutEditPolicy<OfficeFloorModel> {

	@Override
	protected CreateCommand<?, ?> createCreateComand(
			OfficeFloorModel parentModel, Object newModel, Point location) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO implement");
	}

}