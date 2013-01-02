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

import net.officefloor.eclipse.skin.office.OfficeTaskFigure;
import net.officefloor.eclipse.skin.office.OfficeTaskFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingGridLayout;
import net.officefloor.model.office.OfficeTaskModel;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;

/**
 * Standard {@link OfficeTaskFigure}.
 * 
 * @author Daniel Sagenschneider
 */
public class StandardOfficeTaskFigure extends AbstractOfficeFloorFigure
		implements OfficeTaskFigure {

	/**
	 * {@link OfficeTaskModel} name.
	 */
	private final Label taskName;

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link OfficeTaskFigureContext}.
	 */
	public StandardOfficeTaskFigure(OfficeTaskFigureContext context) {

		// Create the figure
		Figure figure = new Figure();
		NoSpacingGridLayout figureLayout = new NoSpacingGridLayout(2);
		figure.setLayoutManager(figureLayout);

		// Create the task name
		this.taskName = new Label(context.getOfficeTaskName());
		figure.add(this.taskName);

		// Create the container for child connectors
		Figure contentPane = new Figure();
		NoSpacingGridLayout contentPaneLayout = new NoSpacingGridLayout(1);
		contentPaneLayout.verticalSpacing = 2;
		contentPane.setLayoutManager(contentPaneLayout);
		figure.add(contentPane);

		// Specify the figure
		this.setFigure(figure);
		this.setContentPane(contentPane);
	}

	/*
	 * ===================== OfficeTaskFigure ======================
	 */

	@Override
	public void setOfficeTaskName(String officeTaskName) {
		this.taskName.setText(officeTaskName);
	}

}