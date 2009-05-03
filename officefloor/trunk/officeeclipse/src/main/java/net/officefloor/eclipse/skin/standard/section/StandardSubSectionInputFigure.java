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
package net.officefloor.eclipse.skin.standard.section;

import net.officefloor.eclipse.skin.section.SubSectionInputFigure;
import net.officefloor.eclipse.skin.section.SubSectionInputFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.figure.SubRoomItemFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.section.SubSectionOutputToSubSectionInputModel;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;

/**
 * Standard {@link SubSectionInputFigure}.
 * 
 * @author Daniel
 */
public class StandardSubSectionInputFigure extends AbstractOfficeFloorFigure
		implements SubSectionInputFigure {

	/**
	 * {@link Figure}.
	 */
	private final SubRoomItemFigure figure;

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link SubSectionInputFigureContext}.
	 */
	public StandardSubSectionInputFigure(SubSectionInputFigureContext context) {
		this.figure = new SubRoomItemFigure(context.getSubSectionInputName(),
				context.isPublic(), ConnectorDirection.WEST,
				ColorConstants.black);
		ConnectionAnchor anchor = this.figure.getConnectionAnchor();
		this.registerConnectionAnchor(
				SubSectionOutputToSubSectionInputModel.class, anchor);
		this.setFigure(figure);
	}

	@Override
	public void setIsPublic(boolean isPublic) {
		this.figure.setIsPublic(isPublic);
	}
}