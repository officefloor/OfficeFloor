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

import net.officefloor.eclipse.common.editparts.CheckBoxEditPart;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.room.ExternalEscalationFigure;
import net.officefloor.eclipse.skin.room.ExternalEscalationFigureContext;
import net.officefloor.eclipse.skin.room.ExternalFlowFigure;
import net.officefloor.eclipse.skin.room.ExternalFlowFigureContext;
import net.officefloor.eclipse.skin.room.ExternalManagedObjectFigure;
import net.officefloor.eclipse.skin.room.ExternalManagedObjectFigureContext;
import net.officefloor.eclipse.skin.room.RoomFigureFactory;
import net.officefloor.eclipse.skin.room.SubRoomEscalationFigureContext;
import net.officefloor.eclipse.skin.room.SubRoomFigureContext;
import net.officefloor.eclipse.skin.room.SubRoomInputFlowFigureContext;
import net.officefloor.eclipse.skin.room.SubRoomManagedObjectFigureContext;
import net.officefloor.eclipse.skin.room.SubRoomOutputFlowFigureContext;
import net.officefloor.eclipse.skin.standard.OfficeFloorFigureImpl;

import org.eclipse.draw2d.CheckBox;

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
	public ExternalEscalationFigure createExternalEscalationFigure(
			ExternalEscalationFigureContext context) {
		return new StandardExternalEscalationFigure(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.skin.room.RoomFigureFactory#createExternalFlowFigure
	 * (net.officefloor.eclipse.skin.room.ExternalFlowFigureContext)
	 */
	@Override
	public ExternalFlowFigure createExternalFlowFigure(
			ExternalFlowFigureContext context) {
		return new StandardExternalFlowFigure(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.skin.room.RoomFigureFactory#
	 * createExternalManagedObjectFigure
	 * (net.officefloor.eclipse.skin.room.ExternalManagedObjectFigureContext)
	 */
	@Override
	public ExternalManagedObjectFigure createExternalManagedObjectFigure(
			ExternalManagedObjectFigureContext context) {
		return new StandardExternalManagedObjectFigure(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.skin.room.RoomFigureFactory#createSubRoomFigure
	 * (net.officefloor.eclipse.skin.room.SubRoomFigureContext)
	 */
	@Override
	public net.officefloor.eclipse.skin.room.SubRoomFigure createSubRoomFigure(
			SubRoomFigureContext context) {
		return new StandardSubRoomFigure(context);
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
		return new OfficeFloorFigureImpl(new SubRoomEscalationFigure(context
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
	public net.officefloor.eclipse.skin.room.SubRoomInputFlowFigure createSubRoomInputFlowFigure(
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
		return new SubRoomInputFlowFigureImpl(new SubRoomInputFlowFigure(
				context.getSubRoomInputFlowName(), publicCheckBox.getFigure()),
				(CheckBox) publicCheckBox.getFigure());
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
		return new OfficeFloorFigureImpl(new SubRoomManagedObjectFigure(context
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
		return new OfficeFloorFigureImpl(new SubRoomOutputFlowFigure(context
				.getSubRoomOutputFlowName()));
	}

}
