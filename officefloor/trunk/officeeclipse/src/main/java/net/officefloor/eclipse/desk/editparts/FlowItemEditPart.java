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
import net.officefloor.eclipse.common.editparts.CheckBoxEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.common.figure.FreeformWrapperFigure;
import net.officefloor.eclipse.desk.figure.FlowItemFigure;
import net.officefloor.model.desk.DeskTaskToFlowItemModel;
import net.officefloor.model.desk.DeskWorkToFlowItemModel;
import net.officefloor.model.desk.FlowItemModel;
import net.officefloor.model.desk.FlowItemModel.FlowItemEvent;

import org.eclipse.draw2d.IFigure;

/**
 * {@link org.eclipse.gef.EditPart} for the
 * {@link net.officefloor.model.desk.FlowItemModel}.
 * 
 * @author Daniel
 */
public class FlowItemEditPart extends
		AbstractOfficeFloorNodeEditPart<FlowItemModel> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	protected IFigure createFigure() {

		// Create the check box to indicate if public
		CheckBoxEditPart publicCheckBox = new CheckBoxEditPart(
				FlowItemEditPart.this.getCastedModel().getIsPublic()) {
			protected void checkBoxStateChanged(boolean isChecked) {
				// Specify if public
				FlowItemEditPart.this.getCastedModel().setIsPublic(isChecked);
			}
		};

		// Create the figure
		FlowItemFigure figure = new FlowItemFigure(this.getCastedModel()
				.getId(), publicCheckBox.getFigure());

		// Return the figure (useable as a freeform figure)
		return new FreeformWrapperFigure(figure);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#populateModelChildren(java.util.List)
	 */
	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel().getOutputs());
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
		// Add work that this is initial flow
		DeskWorkToFlowItemModel work = this.getCastedModel().getDeskWork();
		if (work != null) {
			models.add(work);
		}

		// Add desk task
		DeskTaskToFlowItemModel task = this.getCastedModel().getDeskTask();
		if (task != null) {
			models.add(task);
		}

		// Add flow inputs
		models.addAll(this.getCastedModel().getInputs());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#populatePropertyChangeHandlers(java.util.List)
	 */
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler> handlers) {
		handlers.add(new PropertyChangeHandler<FlowItemEvent>(FlowItemEvent
				.values()) {
			protected void handlePropertyChange(FlowItemEvent property,
					PropertyChangeEvent evt) {
				switch (property) {
				case ADD_INPUT:
				case REMOVE_INPUT:
				case CHANGE_DESK_TASK:
					FlowItemEditPart.this.refreshTargetConnections();
					break;
				}
			}
		});
	}

}
