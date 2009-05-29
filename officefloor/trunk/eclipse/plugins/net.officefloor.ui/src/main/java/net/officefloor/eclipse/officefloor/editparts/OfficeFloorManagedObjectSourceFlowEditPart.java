/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.eclipse.officefloor.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectSourceFlowFigureContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceFlowModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceFlowModel.OfficeFloorManagedObjectSourceFlowEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link OfficeFloorManagedObjectSourceFlowModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorManagedObjectSourceFlowEditPart
		extends
		AbstractOfficeFloorEditPart<OfficeFloorManagedObjectSourceFlowModel, OfficeFloorManagedObjectSourceFlowEvent, OfficeFloorFigure>
		implements OfficeFloorManagedObjectSourceFlowFigureContext {

	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFloorFigureFactory()
				.createOfficeFloorManagedObjectSourceFlowFigure(this);
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		EclipseUtil.addToList(models, this.getCastedModel()
				.getDeployedOfficeInput());
	}

	@Override
	protected Class<OfficeFloorManagedObjectSourceFlowEvent> getPropertyChangeEventType() {
		return OfficeFloorManagedObjectSourceFlowEvent.class;
	}

	@Override
	protected void handlePropertyChange(
			OfficeFloorManagedObjectSourceFlowEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_DEPLOYED_OFFICE_INPUT:
			OfficeFloorManagedObjectSourceFlowEditPart.this
					.refreshSourceConnections();
			break;
		}
	}

	/*
	 * ================ ManagedObjectTaskFlowFigureContext ====================
	 */

	@Override
	public String getOfficeFloorManagedObjectSourceFlowName() {
		return this.getCastedModel()
				.getOfficeFloorManagedObjectSourceFlowName();
	}

	@Override
	public String getInitialTaskName() {
		// return this.getCastedModel().getInitialTaskName();
		return null;
	}

	@Override
	public String getInitialWorkName() {
		// return this.getCastedModel().getInitialWorkName();
		return null;
	}

}