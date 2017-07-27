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
package net.officefloor.eclipse.section.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.eclipse.gef.EditPart;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.section.FunctionEscalationFigureContext;
import net.officefloor.model.section.FunctionEscalationModel;
import net.officefloor.model.section.FunctionEscalationModel.FunctionEscalationEvent;
import net.officefloor.model.section.FunctionEscalationToExternalFlowModel;
import net.officefloor.model.section.FunctionEscalationToFunctionModel;

/**
 * {@link EditPart} for the {@link FunctionEscalationModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class FunctionEscalationEditPart
		extends AbstractOfficeFloorEditPart<FunctionEscalationModel, FunctionEscalationEvent, OfficeFloorFigure>
		implements FunctionEscalationFigureContext {

	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getSectionFigureFactory().createFunctionEscalationFigure(this);
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		// Function
		FunctionEscalationToFunctionModel function = this.getCastedModel().getFunction();
		if (function != null) {
			models.add(function);
		}

		// External flow
		FunctionEscalationToExternalFlowModel extFlow = this.getCastedModel().getExternalFlow();
		if (extFlow != null) {
			models.add(extFlow);
		}
	}

	@Override
	protected Class<FunctionEscalationEvent> getPropertyChangeEventType() {
		return FunctionEscalationEvent.class;
	}

	@Override
	protected void handlePropertyChange(FunctionEscalationEvent property, PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_FUNCTION:
		case CHANGE_EXTERNAL_FLOW:
			FunctionEscalationEditPart.this.refreshSourceConnections();
			break;

		case CHANGE_ESCALATION_TYPE:
			// Non visual change
			break;
		}
	}

	/*
	 * ================== FunctionEscalationFigureContext ================
	 */

	@Override
	public String getEscalationType() {
		return this.getCastedModel().getEscalationType();
	}

}