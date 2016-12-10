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
package net.officefloor.eclipse.skin.standard.office;

import net.officefloor.eclipse.skin.office.DutyFigure;
import net.officefloor.eclipse.skin.office.DutyFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.eclipse.skin.standard.figure.LabelConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingGridLayout;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingToolbarLayout;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.office.OfficeTaskToPostDutyModel;
import net.officefloor.model.office.OfficeTaskToPreDutyModel;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;

/**
 * Standard {@link DutyFigure}.
 * 
 * @author Daniel Sagenschneider
 */
public class StandardDutyFigure extends AbstractOfficeFloorFigure implements
		DutyFigure {

	/**
	 * Duty name.
	 */
	private final Label dutyName;

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link DutyFigureContext}.
	 */
	public StandardDutyFigure(DutyFigureContext context) {

		// Create the figure
		Figure figure = new Figure();
		figure.setLayoutManager(new NoSpacingGridLayout(1));

		// Create the duty figure
		LabelConnectorFigure duty = new LabelConnectorFigure(
				context.getDutyName(), ConnectorDirection.WEST,
				StandardOfficeFloorColours.BLACK());
		figure.add(duty);
		this.dutyName = duty.getLabel();

		// Register anchor to office tasks
		ConnectionAnchor anchor = duty.getConnectionAnchor();
		this.registerConnectionAnchor(OfficeTaskToPreDutyModel.class, anchor);
		this.registerConnectionAnchor(OfficeTaskToPostDutyModel.class, anchor);

		// Create the content pane
		Figure contentPane = new Figure();
		contentPane.setLayoutManager(new NoSpacingToolbarLayout(false));
		figure.add(contentPane);

		// Specify figure and content pane
		this.setFigure(figure);
		this.setContentPane(contentPane);
	}

	/*
	 * ===================== DutyFigure ============================
	 */

	@Override
	public void setDutyName(String dutyName) {
		this.dutyName.setText(dutyName);
	}

}