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
package net.officefloor.eclipse.skin.standard.office;

import net.officefloor.eclipse.skin.office.DeskFigure;
import net.officefloor.eclipse.skin.office.DeskFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingToolbarLayout;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;

/**
 * Standard {@link DeskFigure}.
 * 
 * @author Daniel
 */
public class StandardDeskFigure extends AbstractOfficeFloorFigure implements
		DeskFigure {

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link DeskFigureContext}.
	 */
	public StandardDeskFigure(DeskFigureContext context) {

		// Determine desk name with rooms prefix
		String[] ancestorRoomNames = context.getRoomNames();
		StringBuilder deskName = new StringBuilder();
		for (int i = 0; i < ancestorRoomNames.length; i++) {
			deskName.append(ancestorRoomNames[i]);
			deskName.append(".");
		}
		deskName.append(context.getDeskName());

		// Create the figure
		Figure figure = new Figure();
		figure.setLayoutManager(new NoSpacingToolbarLayout(false));

		// Provide the desk name
		Label deskLabel = new Label(deskName.toString());
		deskLabel.setLayoutManager(new NoSpacingToolbarLayout(true));
		figure.add(deskLabel);

		// Content pane
		Figure contentPane = new Figure();
		contentPane.setLayoutManager(new NoSpacingToolbarLayout(false));
		contentPane.setBorder(new MarginBorder(2, 20, 2, 2));
		figure.add(contentPane);

		// Specify figures
		this.setFigure(figure);
		this.setContentPane(contentPane);
	}
}
