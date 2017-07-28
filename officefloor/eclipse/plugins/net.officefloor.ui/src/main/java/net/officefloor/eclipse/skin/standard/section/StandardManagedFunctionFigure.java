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

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.geometry.Insets;

import net.officefloor.eclipse.skin.section.ManagedFunctionFigure;
import net.officefloor.eclipse.skin.section.ManagedFunctionFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.eclipse.skin.standard.figure.LabelConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingToolbarLayout;
import net.officefloor.model.section.ManagedFunctionModel;
import net.officefloor.model.section.ManagedFunctionToFunctionModel;

/**
 * Standard {@link ManagedFunctionFigure}.
 * 
 * @author Daniel Sagenschneider
 */
public class StandardManagedFunctionFigure extends AbstractOfficeFloorFigure implements ManagedFunctionFigure {

	/**
	 * Name of the {@link ManagedFunctionModel}.
	 */
	private final Label managedFunctionName;

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link ManagedFunctionFigureContext}.
	 */
	public StandardManagedFunctionFigure(ManagedFunctionFigureContext context) {

		// Create the figure
		Figure figure = new Figure();
		figure.setLayoutManager(new NoSpacingToolbarLayout(false));

		// Function name
		LabelConnectorFigure nameFigure = new LabelConnectorFigure(context.getManagedFunctionName(),
				ConnectorDirection.EAST, StandardOfficeFloorColours.BLACK());
		this.managedFunctionName = nameFigure.getLabel();
		this.registerConnectionAnchor(ManagedFunctionToFunctionModel.class, nameFigure.getConnectionAnchor());
		figure.add(nameFigure);

		// Content Pane
		Figure contentPane = new Figure();
		contentPane.setLayoutManager(new NoSpacingToolbarLayout(false));
		contentPane.setBorder(new MarginBorder(new Insets(0, 20, 0, 0)));
		figure.add(contentPane);

		// Specify figures
		this.setFigure(figure);
		this.setContentPane(contentPane);
	}

	/*
	 * ==================== ManagedFunctionFigure ========================
	 */

	@Override
	public void setManagedFunctionName(String workTaskName) {
		this.managedFunctionName.setText(workTaskName);
	}

}