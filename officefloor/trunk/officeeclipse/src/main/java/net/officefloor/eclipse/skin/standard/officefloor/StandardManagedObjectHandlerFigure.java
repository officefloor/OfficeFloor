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
package net.officefloor.eclipse.skin.standard.officefloor;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;

import net.officefloor.eclipse.skin.officefloor.ManagedObjectHandlerFigure;
import net.officefloor.eclipse.skin.officefloor.ManagedObjectHandlerFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingGridLayout;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingToolbarLayout;
import net.officefloor.model.officefloor.ManagedObjectHandlerInstanceModel;

/**
 * Standard {@link ManagedObjectHandlerFigure}.
 * 
 * @author Daniel
 */
public class StandardManagedObjectHandlerFigure extends
		AbstractOfficeFloorFigure implements ManagedObjectHandlerFigure {

	/**
	 * {@link ManagedObjectHandlerFigureContext}.
	 */
	private final ManagedObjectHandlerFigureContext context;

	/**
	 * {@link Figure} for {@link ManagedObjectHandlerInstanceModel} being
	 * assigned.
	 */
	private final Ellipse assignedInstance;

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link ManagedObjectHandlerFigureContext}.
	 */
	public StandardManagedObjectHandlerFigure(
			ManagedObjectHandlerFigureContext context) {
		this.context = context;

		// Create the figure
		Figure figure = new Figure();
		figure.setLayoutManager(new NoSpacingGridLayout(1));

		// Create the header
		Figure header = new Figure();
		NoSpacingGridLayout headerLayout = new NoSpacingGridLayout(2);
		headerLayout.horizontalSpacing = 3;
		header.setLayoutManager(headerLayout);
		figure.add(header);

		// Specify the handler name
		Label name = new Label(this.context.getManagedObjectHandlerName());
		name.setLayoutManager(new NoSpacingToolbarLayout(true));
		header.add(name);

		// Specify instance assigned
		this.assignedInstance = new Ellipse();
		this.assignedInstance.setBackgroundColor(ColorConstants.black);
		this.assignedInstance.setForegroundColor(ColorConstants.black);
		this.assignedInstance.setSize(5, 5);
		header.add(this.assignedInstance);

		// Specify state
		this.handlerInstanceChanged();

		// Provide content pane
		Figure contentPane = new Figure();
		contentPane.setLayoutManager(new NoSpacingToolbarLayout(false));
		contentPane.setBorder(new MarginBorder(0, 20, 0, 0));
		figure.add(contentPane);

		// Specify the figures
		this.setFigure(figure);
		this.setContentPane(contentPane);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.skin.officefloor.ManagedObjectHandlerFigure#
	 * handlerInstanceChanged()
	 */
	@Override
	public void handlerInstanceChanged() {
		// Specify whether instance assigned
		boolean isInstanceAssigned = this.context.isHandlerInstanceAssigned();
		this.assignedInstance.setVisible(isInstanceAssigned);
	}

}
