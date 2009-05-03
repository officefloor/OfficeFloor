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
import net.officefloor.model.office.OfficeModel.OfficeEvent;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.editpolicies.LayoutEditPolicy;

/**
 * {@link EditPart} for the {@link OfficeModel}.
 * 
 * @author Daniel
 */
public class OfficeEditPart extends
		AbstractOfficeFloorDiagramEditPart<OfficeModel> {

	@Override
	protected OfficeFloorLayoutEditPolicy<?> createLayoutEditPolicy() {
		return new OfficeLayoutEditPolicy();
	}

	@Override
	protected void populateChildren(List<Object> childModels) {
		OfficeModel office = this.getCastedModel();
		childModels.addAll(office.getOfficeSections());
		childModels.addAll(office.getExternalManagedObjects());
		childModels.addAll(office.getOfficeTeams());
		childModels.addAll(office.getOfficeAdministrators());
		childModels.addAll(office.getOfficeEscalations());
	}

	@Override
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler<?>> handlers) {
		handlers.add(new PropertyChangeHandler<OfficeEvent>(OfficeEvent
				.values()) {
			@Override
			protected void handlePropertyChange(OfficeEvent property,
					PropertyChangeEvent evt) {
				switch (property) {
				case ADD_OFFICE_SECTION:
				case REMOVE_OFFICE_SECTION:
				case ADD_OFFICE_TEAM:
				case REMOVE_OFFICE_TEAM:
				case ADD_OFFICE_ADMINISTRATOR:
				case REMOVE_OFFICE_ADMINISTRATOR:
				case ADD_EXTERNAL_MANAGED_OBJECT:
				case REMOVE_EXTERNAL_MANAGED_OBJECT:
				case ADD_OFFICE_ESCALATION:
				case REMOVE_OFFICE_ESCALATION:
					OfficeEditPart.this.refreshChildren();
					break;
				}
			}
		});
	}
}

/**
 * {@link LayoutEditPolicy} for the
 * {@link OfficeModel}.
 */
class OfficeLayoutEditPolicy extends OfficeFloorLayoutEditPolicy<OfficeModel> {

	@Override
	protected CreateCommand<?, ?> createCreateComand(OfficeModel parentModel,
			Object newModel, Point location) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO implement");
	}

}