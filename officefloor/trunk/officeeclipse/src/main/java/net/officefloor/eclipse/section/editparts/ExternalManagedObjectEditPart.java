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
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.section.ExternalManagedObjectFigureContext;
import net.officefloor.model.section.ExternalManagedObjectModel;
import net.officefloor.model.section.ExternalManagedObjectModel.ExternalManagedObjectEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link ExternalManagedObjectEditPart}.
 * 
 * @author Daniel
 */
public class ExternalManagedObjectEditPart
		extends
		AbstractOfficeFloorEditPart<ExternalManagedObjectModel, ExternalManagedObjectEvent, OfficeFloorFigure>
		implements ExternalManagedObjectFigureContext {

	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getRoomFigureFactory()
				.createExternalManagedObjectFigure(this);
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		models.addAll(this.getCastedModel().getSubSectionObjects());
	}

	@Override
	protected Class<ExternalManagedObjectEvent> getPropertyChangeEventType() {
		return ExternalManagedObjectEvent.class;
	}

	@Override
	protected void handlePropertyChange(ExternalManagedObjectEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case ADD_SUB_SECTION_OBJECT:
		case REMOVE_SUB_SECTION_OBJECT:
			ExternalManagedObjectEditPart.this.refreshTargetConnections();
			break;
		}
	}

	/*
	 * =================== ExternalManagedObjectFigureContext =================
	 */

	@Override
	public String getExternalManagedObjectName() {
		return this.getCastedModel().getExternalManagedObjectName();
	}

}