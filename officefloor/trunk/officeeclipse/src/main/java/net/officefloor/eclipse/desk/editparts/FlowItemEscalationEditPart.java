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

import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.desk.figure.FlowItemOutputFigure;
import net.officefloor.model.desk.FlowItemEscalationModel;
import net.officefloor.model.desk.FlowItemEscalationModel.FlowItemEscalationEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link FlowItemEscalationModel}.
 * 
 * @author Daniel
 */
public class FlowItemEscalationEditPart extends
		AbstractOfficeFloorEditPart<FlowItemEscalationModel> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#populatePropertyChangeHandlers(java.util.List)
	 */
	@Override
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler<?>> handlers) {
		handlers.add(new PropertyChangeHandler<FlowItemEscalationEvent>(
				FlowItemEscalationEvent.values()) {
			@Override
			protected void handlePropertyChange(
					FlowItemEscalationEvent property, PropertyChangeEvent evt) {
				// TODO provide connection handling
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

		// Obtain simple name of escalation
		String escalationType = this.getCastedModel().getEscalationType();
		String simpleType = escalationType;
		if (simpleType.indexOf('.') > 0) {
			simpleType = simpleType.substring(simpleType.lastIndexOf('.') + 1);
		}

		// Create the figure
		FlowItemOutputFigure figure = new FlowItemOutputFigure(simpleType);
		figure.setToolTip(new Label(escalationType));

		// Return the figure
		return figure;
	}

}
