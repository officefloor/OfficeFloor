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
package net.officefloor.eclipse.section.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.common.editparts.RemovableEditPart;
import net.officefloor.eclipse.section.operations.RemoveSubSectionOperation;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.section.SubSectionFigureContext;
import net.officefloor.model.section.SubSectionModel;
import net.officefloor.model.section.SubSectionModel.SubSectionEvent;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;

/**
 * {@link EditPart} for the {@link SubSectionModel}.
 * 
 * @author Daniel
 */
public class SubSectionEditPart extends
		AbstractOfficeFloorEditPart<SubSectionModel, OfficeFloorFigure>
		implements RemovableEditPart, SubSectionFigureContext {

	@Override
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler<?>> handlers) {
		handlers.add(new PropertyChangeHandler<SubSectionEvent>(SubSectionEvent
				.values()) {
			@Override
			protected void handlePropertyChange(SubSectionEvent property,
					PropertyChangeEvent evt) {
				switch (property) {
				case ADD_SUB_SECTION_INPUT:
				case REMOVE_SUB_SECTION_INPUT:
				case ADD_SUB_SECTION_OUTPUT:
				case REMOVE_SUB_SECTION_OUTPUT:
				case ADD_SUB_SECTION_OBJECT:
				case REMOVE_SUB_SECTION_OBJECT:
					SubSectionEditPart.this.refreshChildren();
					break;
				}
			}
		});
	}

	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getRoomFigureFactory()
				.createSubSectionFigure(this);
	}

	@Override
	protected boolean isFreeformFigure() {
		return true;
	}

	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel().getSubSectionInputs());
		childModels.addAll(this.getCastedModel().getSubSectionOutputs());
		childModels.addAll(this.getCastedModel().getSubSectionObjects());
	}

	@Override
	public Operation getRemoveOperation() {
		return new RemoveSubSectionOperation();
	}

	@Override
	protected Command handleDoubleClick(Request request) {

		// TODO handle opening the sub section

		// // Determine if open the desk
		// String deskPath = this.getCastedModel().getDesk();
		// if (deskPath != null) {
		// // Is desk, so open it
		// this.openClasspathFile(deskPath, DeskEditor.EDITOR_ID);
		// return null;
		// }
		//
		// // Not a desk, so must be a room
		// String roomPath = this.getCastedModel().getRoom();
		// this.openClasspathFile(roomPath, RoomEditor.EDITOR_ID);

		// No command, as no need for undo
		return null;
	}

	/*
	 * ================= SubRoomFigureContext =======================
	 */

	@Override
	public String getSubSectionName() {
		return this.getCastedModel().getSubSectionName();
	}

}