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

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.desk.ExternalFlowFigureContext;
import net.officefloor.model.desk.ExternalFlowModel;
import net.officefloor.model.desk.ExternalFlowModel.ExternalFlowEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link ExternalFlowModel}.
 * 
 * @author Daniel
 */
public class ExternalFlowEditPart
		extends
		AbstractOfficeFloorEditPart<ExternalFlowModel, ExternalFlowEvent, OfficeFloorFigure>
		implements ExternalFlowFigureContext {

	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getDeskFigureFactory()
				.createExternalFlowFigure(this);
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		models.addAll(this.getCastedModel().getTaskFlows());
		models.addAll(this.getCastedModel().getTaskEscalations());
		models.addAll(this.getCastedModel().getPreviousTasks());
	}

	@Override
	protected Class<ExternalFlowEvent> getPropertyChangeEventType() {
		return ExternalFlowEvent.class;
	}

	@Override
	protected void handlePropertyChange(ExternalFlowEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case ADD_TASK_FLOW:
		case REMOVE_TASK_FLOW:
		case ADD_TASK_ESCALATION:
		case REMOVE_TASK_ESCALATION:
		case ADD_PREVIOUS_TASK:
		case REMOVE_PREVIOUS_TASK:
			ExternalFlowEditPart.this.refreshTargetConnections();
			break;
		}
	}

	/*
	 * ================= ExternalFlowFigureContext =====================
	 */

	@Override
	public String getExternalFlowName() {
		return this.getCastedModel().getExternalFlowName();
	}

}