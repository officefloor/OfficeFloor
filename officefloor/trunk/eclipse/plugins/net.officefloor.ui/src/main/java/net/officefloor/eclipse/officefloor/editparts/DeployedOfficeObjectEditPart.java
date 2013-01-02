/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.eclipse.officefloor.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.skin.officefloor.DeployedOfficeObjectFigure;
import net.officefloor.eclipse.skin.officefloor.DeployedOfficeObjectFigureContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.model.officefloor.DeployedOfficeObjectModel;
import net.officefloor.model.officefloor.DeployedOfficeObjectModel.DeployedOfficeObjectEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link DeployedOfficeObjectModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class DeployedOfficeObjectEditPart
		extends
		AbstractOfficeFloorEditPart<DeployedOfficeObjectModel, DeployedOfficeObjectEvent, DeployedOfficeObjectFigure>
		implements DeployedOfficeObjectFigureContext {

	@Override
	protected DeployedOfficeObjectFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFloorFigureFactory()
				.createDeployedOfficeObjectFigure(this);
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		EclipseUtil.addToList(models, this.getCastedModel()
				.getOfficeFloorManagedObject());
		EclipseUtil.addToList(models, this.getCastedModel()
				.getOfficeFloorInputManagedObject());
	}

	@Override
	protected Class<DeployedOfficeObjectEvent> getPropertyChangeEventType() {
		return DeployedOfficeObjectEvent.class;
	}

	@Override
	protected void handlePropertyChange(DeployedOfficeObjectEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_DEPLOYED_OFFICE_OBJECT_NAME:
			this.getOfficeFloorFigure().setDeployedOfficeObjectName(
					this.getDeployedOfficeObjectName());
			break;

		case CHANGE_OFFICE_FLOOR_MANAGED_OBJECT:
			this.refreshSourceConnections();
			break;

		case CHANGE_OFFICE_FLOOR_INPUT_MANAGED_OBJECT:
			this.refreshSourceConnections();
			break;

		case CHANGE_OBJECT_TYPE:
			// Non visual change
			break;
		}
	}

	/*
	 * ==================== OfficeManagedObjectFigureContext =================
	 */

	@Override
	public String getDeployedOfficeObjectName() {
		return this.getCastedModel().getDeployedOfficeObjectName();
	}

}