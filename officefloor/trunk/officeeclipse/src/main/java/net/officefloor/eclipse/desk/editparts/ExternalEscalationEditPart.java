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
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.model.desk.ExternalEscalationModel;
import net.officefloor.model.desk.ExternalEscalationModel.ExternalEscalationEvent;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link ExternalEscalationModel}.
 * 
 * @author Daniel
 */
public class ExternalEscalationEditPart extends
		AbstractOfficeFloorNodeEditPart<ExternalEscalationModel> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	@Override
	protected IFigure createFigure() {
		Label figure = new Label(this.getCastedModel().getName());
		figure.setBackgroundColor(ColorConstants.lightGray);
		figure.setOpaque(true);
		figure.setBounds(new Rectangle(140, 30, 120, 20));

		// Return figure
		return figure;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#populatePropertyChangeHandlers(java.util.List)
	 */
	@Override
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler<?>> handlers) {
		handlers.add(new PropertyChangeHandler<ExternalEscalationEvent>(
				ExternalEscalationEvent.values()) {
			@Override
			protected void handlePropertyChange(
					ExternalEscalationEvent property, PropertyChangeEvent evt) {
				switch (property) {
				case ADD_HANDLED_ESCALATION:
				case REMOVE_HANDLED_ESCALATION:
					ExternalEscalationEditPart.this.refreshTargetConnections();
					break;
				}
			}
		});
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
		models.addAll(this.getCastedModel().getHandledEscalations());
	}

}
