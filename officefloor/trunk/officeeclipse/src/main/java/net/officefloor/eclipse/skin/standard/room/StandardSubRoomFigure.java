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
package net.officefloor.eclipse.skin.standard.room;

import net.officefloor.eclipse.skin.room.SubRoomFigure;
import net.officefloor.eclipse.skin.room.SubRoomFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.figure.ContainerFigure;

import org.eclipse.swt.graphics.Color;

/**
 * Standard {@link SubRoomFigure}.
 * 
 * @author Daniel
 */
public class StandardSubRoomFigure extends AbstractOfficeFloorFigure implements
		SubRoomFigure {

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link SubRoomFigureContext}.
	 */
	public StandardSubRoomFigure(SubRoomFigureContext context) {

		Color subRoomColour = new Color(null, 130, 255, 150);

		// Create the container for the sub room
		ContainerFigure subRoomFigure = new ContainerFigure(context
				.getSubRoomName(), subRoomColour, 5, false);

		// Specify the figures
		this.setFigure(subRoomFigure);
		this.setContentPane(subRoomFigure.getContentPane());
	}

}
