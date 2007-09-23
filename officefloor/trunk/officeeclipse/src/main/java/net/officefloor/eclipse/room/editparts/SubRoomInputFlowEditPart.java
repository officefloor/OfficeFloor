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

import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart;
import net.officefloor.eclipse.common.editparts.CheckBoxEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.room.figure.SubRoomInputFlowFigure;
import net.officefloor.model.room.SubRoomInputFlowModel;
import net.officefloor.model.room.SubRoomInputFlowModel.SubRoomInputFlowEvent;

import org.eclipse.draw2d.IFigure;

/**
 * {@link org.eclipse.gef.EditPart} for the
 * {@link net.officefloor.model.room.SubRoomInputFlowModel}.
 * 
 * @author Daniel
 */
public class SubRoomInputFlowEditPart extends
		AbstractOfficeFloorNodeEditPart<SubRoomInputFlowModel> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart#populateConnectionSourceModels(java.util.List)
	 */
	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		// Not a source
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart#populateConnectionTargetModels(java.util.List)
	 */
	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		models.addAll(this.getCastedModel().getOutputs());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#populatePropertyChangeHandlers(java.util.List)
	 */
	@Override
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler> handlers) {
		handlers.add(new PropertyChangeHandler<SubRoomInputFlowEvent>(
				SubRoomInputFlowEvent.values()) {
			@Override
			protected void handlePropertyChange(SubRoomInputFlowEvent property,
					PropertyChangeEvent evt) {
				switch (property) {
				case ADD_OUTPUT:
				case REMOVE_OUTPUT:
					SubRoomInputFlowEditPart.this.refreshTargetConnections();
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

		// Create the check box to indicate if public
		CheckBoxEditPart publicCheckBox = new CheckBoxEditPart(
				SubRoomInputFlowEditPart.this.getCastedModel().getIsPublic()) {
			protected void checkBoxStateChanged(boolean isChecked) {
				// Specify if public
				SubRoomInputFlowEditPart.this.getCastedModel().setIsPublic(
						isChecked);
			}
		};

		// Return the figure for the Sub Room Input Flow
		return new SubRoomInputFlowFigure(this.getCastedModel().getName(),
				publicCheckBox.getFigure());
	}

}
