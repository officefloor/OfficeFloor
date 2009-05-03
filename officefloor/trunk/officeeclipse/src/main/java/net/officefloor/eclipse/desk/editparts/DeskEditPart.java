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

import net.officefloor.eclipse.common.commands.CreateCommand;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorDiagramEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.common.editpolicies.OfficeFloorLayoutEditPolicy;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.DeskModel.DeskEvent;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.editpolicies.LayoutEditPolicy;

/**
 * {@link EditPart} for the {@link DeskModel}.
 * 
 * @author Daniel
 */
public class DeskEditPart extends AbstractOfficeFloorDiagramEditPart<DeskModel> {

	@Override
	protected OfficeFloorLayoutEditPolicy<?> createLayoutEditPolicy() {
		return new DeskLayoutEditPolicy();
	}

	@Override
	protected void populateChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel().getWorks());
		childModels.addAll(this.getCastedModel().getExternalManagedObjects());
		childModels.addAll(this.getCastedModel().getTasks());
		childModels.addAll(this.getCastedModel().getExternalFlows());
	}

	@Override
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler<?>> handlers) {
		handlers.add(new PropertyChangeHandler<DeskEvent>(DeskEvent.values()) {
			protected void handlePropertyChange(DeskEvent property,
					PropertyChangeEvent evt) {
				switch (property) {
				case ADD_EXTERNAL_MANAGED_OBJECT:
				case REMOVE_EXTERNAL_MANAGED_OBJECT:
					DeskEditPart.this.refresh();
					break;
				case ADD_WORK:
				case REMOVE_WORK:
					DeskEditPart.this.refresh();
					break;
				case ADD_EXTERNAL_FLOW:
				case REMOVE_EXTERNAL_FLOW:
					DeskEditPart.this.refresh();
					break;
				case ADD_TASK:
				case REMOVE_TASK:
					DeskEditPart.this.refresh();
					break;
				}
			}
		});
	}
}

/**
 * {@link LayoutEditPolicy} for the {@link DeskModel}.
 */
class DeskLayoutEditPolicy extends OfficeFloorLayoutEditPolicy<DeskModel> {

	@Override
	protected CreateCommand<?, ?> createCreateComand(DeskModel parentModel,
			Object newModel, Point location) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO implement");
	}

}