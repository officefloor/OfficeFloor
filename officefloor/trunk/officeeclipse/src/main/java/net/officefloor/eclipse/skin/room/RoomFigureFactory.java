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
import net.officefloor.model.section.ExternalFlowModel;
import net.officefloor.model.section.ExternalManagedObjectModel;
import net.officefloor.model.section.SubSectionInputModel;
import net.officefloor.model.section.SubSectionModel;

import org.eclipse.draw2d.IFigure;

/**
 * Factory to create the {@link IFigure} instances for the skin of the
 * {@link RoomModel}.
 * 
 * @author Daniel
 */
public interface RoomFigureFactory {

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link ExternalFlowModel}.
	 * 
	 * @param context
	 *            {@link ExternalFlowFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	ExternalFlowFigure createExternalFlowFigure(
			ExternalFlowFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link ExternalManagedObjectModel}.
	 * 
	 * @param context
	 *            {@link ExternalManagedObjectFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	ExternalManagedObjectFigure createExternalManagedObjectFigure(
			ExternalManagedObjectFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link SubSectionModel}.
	 * 
	 * @param context
	 *            {@link SubSectionFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	SubRoomFigure createSubRoomFigure(SubRoomFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link SubSectionInputModel}.
	 * 
	 * @param context
	 *            {@link SubSectionInputFigureContext}.
	 * @return {@link SubSectionInputFigure}.
	 */
	SubRoomInputFlowFigure createSubRoomInputFlowFigure(
			SubRoomInputFlowFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link SubSectionObjectModel}.
	 * 
	 * @param context
	 *            {@link SubSectionObjectFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	SubRoomManagedObjectFigure createSubRoomManagedObjectFigure(
			SubRoomManagedObjectFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link SubSectionOutputModel}.
	 * 
	 * @param context
	 *            {@link SubSectionOutputFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	SubRoomOutputFlowFigure createSubRoomOutputFlowFigure(
			SubRoomOutputFlowFigureContext context);

}
