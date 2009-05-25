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
package net.officefloor.eclipse.skin.standard.desk;

import net.officefloor.eclipse.skin.desk.ExternalFlowFigure;
import net.officefloor.eclipse.skin.desk.ExternalFlowFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.figure.LabelConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.desk.ExternalFlowModel;
import net.officefloor.model.desk.TaskEscalationToExternalFlowModel;
import net.officefloor.model.desk.TaskFlowToExternalFlowModel;
import net.officefloor.model.desk.TaskToNextExternalFlowModel;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;

/**
 * Standard {@link ExternalFlowFigure}.
 * 
 * @author Daniel
 */
public class StandardExternalFlowFigure extends AbstractOfficeFloorFigure
		implements ExternalFlowFigure {

	/**
	 * {@link Label} containing the {@link ExternalFlowModel} name.
	 */
	private final Label externalFlowName;

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link ExternalFlowFigureContext}.
	 */
	public StandardExternalFlowFigure(ExternalFlowFigureContext context) {

		LabelConnectorFigure figure = new LabelConnectorFigure(context
				.getExternalFlowName(), ConnectorDirection.WEST,
				ColorConstants.black);
		this.externalFlowName = figure.getLabel();

		// Register anchors
		ConnectionAnchor anchor = figure.getConnectionAnchor();
		this
				.registerConnectionAnchor(TaskToNextExternalFlowModel.class,
						anchor);
		this
				.registerConnectionAnchor(TaskFlowToExternalFlowModel.class,
						anchor);
		this.registerConnectionAnchor(TaskEscalationToExternalFlowModel.class,
				anchor);

		this.setFigure(figure);
	}

	/*
	 * ==================== ExternalFlowFigure ================================
	 */

	@Override
	public void setExternalFlowName(String externalFlowName) {
		this.externalFlowName.setText(externalFlowName);
	}

	@Override
	public IFigure getExternalFlowNameFigure() {
		return this.externalFlowName;
	}

}
