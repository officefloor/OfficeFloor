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
import net.officefloor.eclipse.skin.officefloor.DeployedOfficeInputFigureContext;
import net.officefloor.model.officefloor.DeployedOfficeInputModel;
import net.officefloor.model.officefloor.DeployedOfficeInputModel.DeployedOfficeInputEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link DeployedOfficeInputModel}.
 * 
 * @author Daniel
 */
public class DeployedOfficeInputEditPart
		extends
		AbstractOfficeFloorEditPart<DeployedOfficeInputModel, DeployedOfficeInputEvent, OfficeFloorFigure>
		implements DeployedOfficeInputFigureContext {

	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFloorFigureFactory()
				.createDeployedOfficeInputFigure(this);
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		models.addAll(this.getCastedModel()
				.getOfficeFloorManagedObjectSourceFlows());
	}

	@Override
	protected Class<DeployedOfficeInputEvent> getPropertyChangeEventType() {
		return DeployedOfficeInputEvent.class;
	}

	@Override
	protected void handlePropertyChange(DeployedOfficeInputEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case ADD_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_FLOW:
		case REMOVE_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_FLOW:
			this.refreshTargetConnections();
			break;
		}
	}

	/*
	 * ================== DeployedOfficeInputFigureContext ====================
	 */

	@Override
	public String getOfficeSectionName() {
		return this.getCastedModel().getSectionName();
	}

	@Override
	public String getOfficeSectionInputName() {
		return this.getCastedModel().getSectionInputName();
	}

}