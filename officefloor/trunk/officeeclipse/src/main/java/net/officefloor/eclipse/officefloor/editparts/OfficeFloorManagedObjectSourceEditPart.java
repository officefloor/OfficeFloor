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

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectSourceFigureContext;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceToDeployedOfficeModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceModel.OfficeFloorManagedObjectSourceEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link OfficeFloorManagedObjectSourceModel}.
 * 
 * @author Daniel
 */
public class OfficeFloorManagedObjectSourceEditPart
		extends
		AbstractOfficeFloorEditPart<OfficeFloorManagedObjectSourceModel, OfficeFloorManagedObjectSourceEvent, OfficeFloorFigure>
		implements OfficeFloorManagedObjectSourceFigureContext {

	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFloorFigureFactory()
				.createOfficeFloorManagedObjectSourceFigure(this);
	}

	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel()
				.getOfficeFloorManagedObjectSourceFlows());
		childModels.addAll(this.getCastedModel()
				.getOfficeFloorManagedObjectSourceTeams());
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		OfficeFloorManagedObjectSourceToDeployedOfficeModel conn = this
				.getCastedModel().getManagingOffice();
		if (conn != null) {
			models.add(conn);
		}
	}

	@Override
	protected Class<OfficeFloorManagedObjectSourceEvent> getPropertyChangeEventType() {
		return OfficeFloorManagedObjectSourceEvent.class;
	}

	@Override
	protected void handlePropertyChange(
			OfficeFloorManagedObjectSourceEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_MANAGING_OFFICE:
			OfficeFloorManagedObjectSourceEditPart.this
					.refreshSourceConnections();
			break;
		case ADD_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_FLOW:
		case REMOVE_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_FLOW:
		case ADD_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_TEAM:
		case REMOVE_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_TEAM:
			OfficeFloorManagedObjectSourceEditPart.this.refreshChildren();
			break;
		}
	}

	/*
	 * ================== ManagedObjectSourceFigureContext =====================
	 */

	@Override
	public String getOfficeFloorManagedObjectSourceName() {
		return this.getCastedModel().getOfficeFloorManagedObjectSourceName();
	}

}