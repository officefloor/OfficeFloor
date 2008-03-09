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

import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.model.officefloor.ManagedObjectHandlerLinkProcessModel;
import net.officefloor.model.officefloor.ManagedObjectHandlerLinkProcessModel.ManagedObjectHandlerLinkProcessEvent;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link ManagedObjectHandlerLinkProcessModel}.
 * 
 * @author Daniel
 */
public class ManagedObjectHandlerLinkProcessEditPart extends
		AbstractOfficeFloorEditPart<ManagedObjectHandlerLinkProcessModel> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#populatePropertyChangeHandlers(java.util.List)
	 */
	@Override
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler<?>> handlers) {
		handlers
				.add(new PropertyChangeHandler<ManagedObjectHandlerLinkProcessEvent>(
						ManagedObjectHandlerLinkProcessEvent.values()) {
					@Override
					protected void handlePropertyChange(
							ManagedObjectHandlerLinkProcessEvent property,
							PropertyChangeEvent evt) {
						switch (property) {

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
		// Create the figure
		IFigure figure = new Label(this.getCastedModel().getLinkProcessId());
		figure.setForegroundColor(ColorConstants.red);

		// Return the figure
		return figure;
	}

}
