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

import net.officefloor.eclipse.skin.room.SubRoomInputFlowFigure;
import net.officefloor.eclipse.skin.room.SubRoomInputFlowFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.figure.SubRoomItemFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.room.EscalationToInputFlowModel;
import net.officefloor.model.room.OutputFlowToInputFlowModel;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;

/**
 * Standard {@link SubRoomInputFlowFigure}.
 * 
 * @author Daniel
 */
public class StandardSubRoomInputFlowFigure extends AbstractOfficeFloorFigure
		implements SubRoomInputFlowFigure {

	/**
	 * {@link Figure}.
	 */
	private final SubRoomItemFigure figure;

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link SubRoomInputFlowFigureContext}.
	 */
	public StandardSubRoomInputFlowFigure(SubRoomInputFlowFigureContext context) {
		this.figure = new SubRoomItemFigure(context.getSubRoomInputFlowName(),
				context.isPublic(), ConnectorDirection.WEST,
				ColorConstants.black);
		ConnectionAnchor anchor = this.figure.getConnectionAnchor();
		this.registerConnectionAnchor(EscalationToInputFlowModel.class, anchor);
		this.registerConnectionAnchor(OutputFlowToInputFlowModel.class, anchor);
		this.setFigure(figure);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.skin.room.SubRoomInputFlowFigure#setIsPublic(
	 * boolean)
	 */
	@Override
	public void setIsPublic(boolean isPublic) {
		this.figure.setIsPublic(isPublic);
	}
}
