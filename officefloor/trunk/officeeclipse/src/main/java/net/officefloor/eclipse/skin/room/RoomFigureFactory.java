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
package net.officefloor.eclipse.skin.room;

import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.model.room.ExternalEscalationModel;
import net.officefloor.model.room.ExternalFlowModel;
import net.officefloor.model.room.ExternalManagedObjectModel;
import net.officefloor.model.room.RoomModel;
import net.officefloor.model.room.SubRoomEscalationModel;
import net.officefloor.model.room.SubRoomInputFlowModel;
import net.officefloor.model.room.SubRoomManagedObjectModel;
import net.officefloor.model.room.SubRoomModel;
import net.officefloor.model.room.SubRoomOutputFlowModel;

import org.eclipse.draw2d.IFigure;

/**
 * Factory to create the {@link IFigure} instances for the skin of the
 * {@link RoomModel}.
 * 
 * @author Daniel
 */
public interface RoomFigureFactory {

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link ExternalEscalationModel}.
	 * 
	 * @param context
	 *            {@link ExternalEscalationFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeFloorFigure createExternalEscalationFigure(
			ExternalEscalationFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link ExternalFlowModel}.
	 * 
	 * @param context
	 *            {@link ExternalFlowFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeFloorFigure createExternalFlowFigure(ExternalFlowFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link ExternalManagedObjectModel}.
	 * 
	 * @param context
	 *            {@link ExternalManagedObjectFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeFloorFigure createExternalManagedObjectFigure(
			ExternalManagedObjectFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link SubRoomModel}.
	 * 
	 * @param context
	 *            {@link SubRoomFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeFloorFigure createSubRoomFigure(SubRoomFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link SubRoomEscalationModel}.
	 * 
	 * @param context
	 *            {@link SubRoomFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeFloorFigure createSubRoomEscalationFigure(
			SubRoomEscalationFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link SubRoomInputFlowModel}.
	 * 
	 * @param context
	 *            {@link SubRoomInputFlowFigureContext}.
	 * @return {@link SubRoomInputFlowFigure}.
	 */
	SubRoomInputFlowFigure createSubRoomInputFlowFigure(
			SubRoomInputFlowFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link SubRoomManagedObjectModel}.
	 * 
	 * @param context
	 *            {@link SubRoomManagedObjectFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeFloorFigure createSubRoomManagedObjectFigure(
			SubRoomManagedObjectFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link SubRoomOutputFlowModel}.
	 * 
	 * @param context
	 *            {@link SubRoomOutputFlowFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeFloorFigure createSubRoomOutputFlowFigure(
			SubRoomOutputFlowFigureContext context);

}
