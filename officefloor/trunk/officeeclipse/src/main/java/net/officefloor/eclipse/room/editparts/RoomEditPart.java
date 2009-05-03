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
package net.officefloor.eclipse.room.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.common.commands.CreateCommand;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorDiagramEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.common.editpolicies.OfficeFloorLayoutEditPolicy;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SectionModel.SectionEvent;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link SectionModel}.
 * 
 * @author Daniel
 */
// TODO rename to SectionEditPart
public class RoomEditPart extends
		AbstractOfficeFloorDiagramEditPart<SectionModel> {

	@Override
	protected OfficeFloorLayoutEditPolicy<?> createLayoutEditPolicy() {
		return new RoomLayoutEditPolicy();
	}

	@Override
	protected void populateChildren(List<Object> childModels) {
		SectionModel section = this.getCastedModel();
		childModels.addAll(section.getSubSections());
		childModels.addAll(section.getExternalManagedObjects());
		childModels.addAll(section.getExternalFlows());
	}

	@Override
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler<?>> handlers) {
		handlers.add(new PropertyChangeHandler<SectionEvent>(SectionEvent
				.values()) {
			protected void handlePropertyChange(SectionEvent property,
					PropertyChangeEvent evt) {
				switch (property) {
				case ADD_EXTERNAL_FLOW:
				case REMOVE_EXTERNAL_FLOW:
				case ADD_EXTERNAL_MANAGED_OBJECT:
				case REMOVE_EXTERNAL_MANAGED_OBJECT:
				case ADD_SUB_SECTION:
				case REMOVE_SUB_SECTION:
					RoomEditPart.this.refreshChildren();
					break;
				}
			}
		});
	}
}

/**
 * {@link LayoutEditPolicy} for the {@link RoomModel}.
 */
// TODO rename to SectionLayoutEditPolicy
class RoomLayoutEditPolicy extends OfficeFloorLayoutEditPolicy<SectionModel> {

	@Override
	protected CreateCommand<?, ?> createCreateComand(SectionModel parentModel,
			Object newModel, Point location) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO implement");
	}

}