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
package net.officefloor.eclipse.skin.office;

import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.office.AdministratorModel;
import net.officefloor.model.office.DutyFlowModel;
import net.officefloor.model.office.DutyModel;
import net.officefloor.model.office.ExternalManagedObjectModel;
import net.officefloor.model.office.ExternalTeamModel;
import net.officefloor.model.office.FlowItemModel;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.room.RoomModel;

import org.eclipse.draw2d.IFigure;

/**
 * Factory to create the {@link IFigure} instances for the skin of the
 * {@link OfficeModel}.
 * 
 * @author Daniel
 */
public interface OfficeFigureFactory {

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link AdministratorModel}.
	 * 
	 * @param context
	 *            {@link AdministratorFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	AdministratorFigure createAdministratorFigure(
			AdministratorFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link DeskModel}.
	 * 
	 * @param context
	 *            {@link DeskFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	DeskFigure createDeskFigure(DeskFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link DutyModel}.
	 * 
	 * @param context
	 *            {@link DutyFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	DutyFigure createDutyFigure(DutyFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link DutyFlowModel}.
	 * 
	 * @param context
	 *            {@link DutyFlowFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeFloorFigure createDutyFlowFigure(DutyFlowFigureContext context);

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
	 * Creates the {@link OfficeFloorFigure} for the {@link ExternalTeamModel}.
	 * 
	 * @param context
	 *            {@link ExternalTeamFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	ExternalTeamFigure createExternalTeamFigure(ExternalTeamFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link FlowItemModel}.
	 * 
	 * @param context
	 *            {@link FlowItemFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeFloorFigure createFlowItemFigure(FlowItemFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link RoomModel}.
	 * 
	 * @param context
	 *            {@link RoomFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	RoomFigure createRoomFigure(RoomFigureContext context);

}
