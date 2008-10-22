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
import net.officefloor.eclipse.skin.room.SubRoomOutputFlowFigure;
import net.officefloor.eclipse.skin.room.SubRoomOutputFlowFigureContext;

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
	public net.officefloor.eclipse.skin.room.SubRoomEscalationFigure createSubRoomEscalationFigure(
			SubRoomEscalationFigureContext context) {
		return new StandardSubRoomEscalationFigure(context);
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
		return new StandardSubRoomInputFlowFigure(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.skin.room.RoomFigureFactory#
	 * createSubRoomManagedObjectFigure
	 * (net.officefloor.eclipse.skin.room.SubRoomManagedObjectFigureContext)
	 */
	@Override
	public net.officefloor.eclipse.skin.room.SubRoomManagedObjectFigure createSubRoomManagedObjectFigure(
			SubRoomManagedObjectFigureContext context) {
		return new StandardSubRoomManagedObjectFigure(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.skin.room.RoomFigureFactory#
	 * createSubRoomOutputFlowFigure
	 * (net.officefloor.eclipse.skin.room.SubRoomOutputFlowFigureContext)
	 */
	@Override
	public SubRoomOutputFlowFigure createSubRoomOutputFlowFigure(
			SubRoomOutputFlowFigureContext context) {
		return new StandardSubRoomOutputFlowFigure(context);
	}

}
