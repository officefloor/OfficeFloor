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
package net.officefloor.eclipse.skin.standard.section;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Label;

import net.officefloor.eclipse.skin.section.FunctionFlowFigure;
import net.officefloor.eclipse.skin.section.FunctionFlowFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.eclipse.skin.standard.figure.LabelConnectorFigure;
import net.officefloor.model.section.FunctionFlowModel;
import net.officefloor.model.section.FunctionFlowToExternalFlowModel;
import net.officefloor.model.section.FunctionFlowToFunctionModel;

/**
 * Standard {@link FunctionFlowFigure}.
 * 
 * @author Daniel Sagenschneider
 */
public class StandardFunctionFlowFigure extends AbstractOfficeFloorFigure implements FunctionFlowFigure {

	/**
	 * {@link FunctionFlowModel} name.
	 */
	private final Label functionFlowName;

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link FunctionFlowFigureContext}.
	 */
	public StandardFunctionFlowFigure(FunctionFlowFigureContext context) {
		LabelConnectorFigure connector = new LabelConnectorFigure(context.getFunctionFlowName(),
				ConnectorDirection.EAST, StandardOfficeFloorColours.BLACK());
		this.functionFlowName = connector.getLabel();

		// Register the anchors
		ConnectionAnchor anchor = connector.getConnectionAnchor();
		this.registerConnectionAnchor(FunctionFlowToFunctionModel.class, anchor);
		this.registerConnectionAnchor(FunctionFlowToExternalFlowModel.class, anchor);

		this.setFigure(connector);
	}

	/*
	 * ================= FunctionFlowFigure ============================
	 */

	@Override
	public void setFunctionFlowName(String taskFlowName) {
		this.functionFlowName.setText(taskFlowName);
	}

}