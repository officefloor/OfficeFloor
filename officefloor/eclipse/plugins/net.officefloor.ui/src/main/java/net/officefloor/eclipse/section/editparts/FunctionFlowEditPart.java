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
import net.officefloor.eclipse.skin.section.FunctionFlowFigure;
import net.officefloor.eclipse.skin.section.FunctionFlowFigureContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.model.section.FunctionFlowModel;
import net.officefloor.model.section.FunctionFlowModel.FunctionFlowEvent;

/**
 * {@link EditPart} for the {@link FunctionFlowModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class FunctionFlowEditPart
		extends AbstractOfficeFloorEditPart<FunctionFlowModel, FunctionFlowEvent, FunctionFlowFigure>
		implements FunctionFlowFigureContext {

	@Override
	protected FunctionFlowFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getSectionFigureFactory().createFunctionFlowFigure(this);
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		EclipseUtil.addToList(models, this.getCastedModel().getFunction());
		EclipseUtil.addToList(models, this.getCastedModel().getExternalFlow());
	}

	@Override
	protected Class<FunctionFlowEvent> getPropertyChangeEventType() {
		return FunctionFlowEvent.class;
	}

	@Override
	protected void handlePropertyChange(FunctionFlowEvent property, PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_FUNCTION:
		case CHANGE_EXTERNAL_FLOW:
			FunctionFlowEditPart.this.refreshSourceConnections();
			break;

		case CHANGE_FLOW_NAME:
			this.getOfficeFloorFigure().setFunctionFlowName(this.getFunctionFlowName());
			break;

		case CHANGE_ARGUMENT_TYPE:
		case CHANGE_KEY:
			// Non visual change
			break;
		}
	}

	/*
	 * ======================= FlowItemOutputFigureContext ================
	 */

	@Override
	public String getFunctionFlowName() {
		return this.getCastedModel().getFlowName();
	}

}