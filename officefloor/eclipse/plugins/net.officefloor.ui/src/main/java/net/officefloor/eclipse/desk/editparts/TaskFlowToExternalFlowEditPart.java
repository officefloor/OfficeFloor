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
package net.officefloor.eclipse.desk.editparts;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorConnectionEditPart;
import net.officefloor.eclipse.desk.DeskEditor;
import net.officefloor.eclipse.skin.desk.TaskFlowToExternalFlowFigureContext;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.model.desk.TaskFlowToExternalFlowModel;
import net.officefloor.model.desk.TaskFlowToExternalFlowModel.TaskFlowToExternalFlowEvent;

import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link TaskFlowToExternalFlowModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class TaskFlowToExternalFlowEditPart
		extends
		AbstractOfficeFloorConnectionEditPart<TaskFlowToExternalFlowModel, TaskFlowToExternalFlowEvent>
		implements TaskFlowToExternalFlowFigureContext {

	/*
	 * ============== AbstractOfficeFloorConnectionEditPart ==================
	 */

	@Override
	protected void decorateFigure(PolylineConnection figure) {
		OfficeFloorPlugin.getSkin().getDeskFigureFactory()
				.decorateTaskFlowToExternalFlowFigure(figure, this);
	}

	/*
	 * ============== TaskFlowToExternalFlowFigureContext ===================
	 */

	@Override
	public FlowInstigationStrategyEnum getFlowInstigationStrategy() {
		return DeskEditor.getFlowInstigationStrategy(this.getCastedModel()
				.getLinkType());
	}

}