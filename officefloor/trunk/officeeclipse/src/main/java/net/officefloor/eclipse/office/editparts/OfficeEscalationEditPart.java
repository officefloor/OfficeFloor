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

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.office.OfficeEscalationFigureContext;
import net.officefloor.model.office.OfficeEscalationModel;
import net.officefloor.model.office.OfficeEscalationModel.OfficeEscalationEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link OfficeEscalationModel}.
 * 
 * @author Daniel
 */
public class OfficeEscalationEditPart
		extends
		AbstractOfficeFloorEditPart<OfficeEscalationModel, OfficeEscalationEvent, OfficeFloorFigure>
		implements OfficeEscalationFigureContext {

	/*
	 * ==================== AbstractOfficeFloorEditPart =======================
	 */

	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFigureFactory()
				.createOfficeEscalationFigure(this);
	}

	@Override
	protected Class<OfficeEscalationEvent> getPropertyChangeEventType() {
		return OfficeEscalationEvent.class;
	}

	@Override
	protected void handlePropertyChange(OfficeEscalationEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		// TODO handle property changes
		}
	}

	/*
	 * ================== OfficeEscalationFigureContext ========================
	 */

	@Override
	public String getOfficeEscalationTypeName() {
		return this.getCastedModel().getEscalationType();
	}

}