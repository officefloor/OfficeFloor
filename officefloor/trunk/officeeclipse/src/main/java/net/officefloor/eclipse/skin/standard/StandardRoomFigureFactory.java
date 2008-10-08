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
package net.officefloor.eclipse.skin.standard;

import net.officefloor.eclipse.common.editparts.CheckBoxEditPart;
import net.officefloor.eclipse.common.figure.FreeformWrapperFigure;
import net.officefloor.eclipse.room.figure.SubRoomEscalationFigure;
import net.officefloor.eclipse.room.figure.SubRoomFigure;
import net.officefloor.eclipse.room.figure.SubRoomInputFlowFigure;
import net.officefloor.eclipse.room.figure.SubRoomManagedObjectFigure;
import net.officefloor.eclipse.room.figure.SubRoomOutputFlowFigure;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.room.ExternalEscalationFigureContext;
import net.officefloor.eclipse.skin.room.ExternalFlowFigureContext;
import net.officefloor.eclipse.skin.room.ExternalManagedObjectFigureContext;
import net.officefloor.eclipse.skin.room.RoomFigureFactory;
import net.officefloor.eclipse.skin.room.SubRoomEscalationFigureContext;
import net.officefloor.eclipse.skin.room.SubRoomFigureContext;
import net.officefloor.eclipse.skin.room.SubRoomInputFlowFigureContext;
import net.officefloor.eclipse.skin.room.SubRoomManagedObjectFigureContext;
import net.officefloor.eclipse.skin.room.SubRoomOutputFlowFigureContext;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * Standard {@link RoomFigureFactory}.
 * 
 * @author Daniel
 */
public class StandardRoomFigureFactory implements RoomFigureFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.skin.room.RoomFigureFactory#
	 * createExternalEscalationFigure
	 * (net.officefloor.eclipse.skin.room.ExternalEscalationFigureContext)
	 */
	@Override
	public OfficeFloorFigure createExternalEscalationFigure(
			ExternalEscalationFigureContext context) {
		Label figure = new Label(context.getExternalEscalationName());
		figure.setBackgroundColor(ColorConstants.lightGray);
		figure.setOpaque(true);
		figure.setBounds(new Rectangle(140, 30, 120, 20));

		// Return figure
		return new OfficeFloorFigure(figure);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.skin.room.RoomFigureFactory#createExternalFlowFigure
	 * (net.officefloor.eclipse.skin.room.ExternalFlowFigureContext)
	 */
	@Override
	public OfficeFloorFigure createExternalFlowFigure(
			ExternalFlowFigureContext context) {
		Label figure = new Label(context.getExternalFlowName());
		figure.setBackgroundColor(ColorConstants.lightGray);
		figure.setOpaque(true);
		figure.setBounds(new Rectangle(140, 30, 120, 20));

		// Return figure
		return new OfficeFloorFigure(figure);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.skin.room.RoomFigureFactory#
	 * createExternalManagedObjectFigure
	 * (net.officefloor.eclipse.skin.room.ExternalManagedObjectFigureContext)
	 */
	@Override
	public OfficeFloorFigure createExternalManagedObjectFigure(
			ExternalManagedObjectFigureContext context) {
		Label figure = new Label(context.getExternalManagedObjectName());
		figure.setBackgroundColor(ColorConstants.lightGray);
		figure.setOpaque(true);
		figure.setLayoutManager(new FlowLayout(true));

		// Return figure
		return new OfficeFloorFigure(figure);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.skin.room.RoomFigureFactory#createSubRoomFigure
	 * (net.officefloor.eclipse.skin.room.SubRoomFigureContext)
	 */
	@Override
	public OfficeFloorFigure createSubRoomFigure(SubRoomFigureContext context) {
		return new OfficeFloorFigure(new FreeformWrapperFigure(
				new SubRoomFigure(context.getSubRoomName())));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.skin.room.RoomFigureFactory#
	 * createSubRoomEscalationFigure
	 * (net.officefloor.eclipse.skin.room.SubRoomEscalationFigureContext)
	 */
	@Override
	public OfficeFloorFigure createSubRoomEscalationFigure(
			SubRoomEscalationFigureContext context) {
		return new OfficeFloorFigure(new SubRoomEscalationFigure(context
				.getSubRoomEscalationName()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.skin.room.RoomFigureFactory#
	 * createSubRoomInputFlowFigure
	 * (net.officefloor.eclipse.skin.room.SubRoomInputFlowFigureContext)
	 */
	@Override
	public OfficeFloorFigure createSubRoomInputFlowFigure(
			final SubRoomInputFlowFigureContext context) {

		// Create the check box to indicate if public
		CheckBoxEditPart publicCheckBox = new CheckBoxEditPart(context
				.isPublic()) {
			protected void checkBoxStateChanged(boolean isChecked) {
				// Specify if public
				context.setIsPublic(isChecked);
			}
		};

		// Return the figure for the Sub Room Input Flow
		return new OfficeFloorFigure(new SubRoomInputFlowFigure(context
				.getSubRoomInputFlowName(), publicCheckBox.getFigure()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.skin.room.RoomFigureFactory#
	 * createSubRoomManagedObjectFigure
	 * (net.officefloor.eclipse.skin.room.SubRoomManagedObjectFigureContext)
	 */
	@Override
	public OfficeFloorFigure createSubRoomManagedObjectFigure(
			SubRoomManagedObjectFigureContext context) {
		return new OfficeFloorFigure(new SubRoomManagedObjectFigure(context
				.getSubRoomManagedObjectName()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.skin.room.RoomFigureFactory#
	 * createSubRoomOutputFlowFigure
	 * (net.officefloor.eclipse.skin.room.SubRoomOutputFlowFigureContext)
	 */
	@Override
	public OfficeFloorFigure createSubRoomOutputFlowFigure(
			SubRoomOutputFlowFigureContext context) {
		return new OfficeFloorFigure(new SubRoomOutputFlowFigure(context
				.getSubRoomOutputFlowName()));
	}

}
