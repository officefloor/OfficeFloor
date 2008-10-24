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

import net.officefloor.eclipse.skin.office.AdministratorFigure;
import net.officefloor.eclipse.skin.office.AdministratorFigureContext;
import net.officefloor.eclipse.skin.office.DeskFigure;
import net.officefloor.eclipse.skin.office.DeskFigureContext;
import net.officefloor.eclipse.skin.office.DutyFigure;
import net.officefloor.eclipse.skin.office.DutyFigureContext;
import net.officefloor.eclipse.skin.office.DutyFlowFigure;
import net.officefloor.eclipse.skin.office.DutyFlowFigureContext;
import net.officefloor.eclipse.skin.office.ExternalManagedObjectFigure;
import net.officefloor.eclipse.skin.office.ExternalManagedObjectFigureContext;
import net.officefloor.eclipse.skin.office.ExternalTeamFigure;
import net.officefloor.eclipse.skin.office.ExternalTeamFigureContext;
import net.officefloor.eclipse.skin.office.FlowItemAdministrationJoinPointFigure;
import net.officefloor.eclipse.skin.office.FlowItemFigure;
import net.officefloor.eclipse.skin.office.FlowItemFigureContext;
import net.officefloor.eclipse.skin.office.OfficeFigureFactory;
import net.officefloor.eclipse.skin.office.RoomFigure;
import net.officefloor.eclipse.skin.office.RoomFigureContext;

/**
 * Standard {@link OfficeFigureFactory}.
 * 
 * @author Daniel
 */
public class StandardOfficeFigureFactory implements OfficeFigureFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.skin.office.OfficeFigureFactory#
	 * createAdministratorFigure
	 * (net.officefloor.eclipse.skin.office.AdministratorFigureContext)
	 */
	@Override
	public AdministratorFigure createAdministratorFigure(
			AdministratorFigureContext context) {
		return new StandardAdministratorFigure(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.skin.office.OfficeFigureFactory#createDeskFigure
	 * (net.officefloor.eclipse.skin.office.DeskFigureContext)
	 */
	@Override
	public DeskFigure createDeskFigure(DeskFigureContext context) {
		return new StandardDeskFigure(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.skin.office.OfficeFigureFactory#createDutyFigure
	 * (net.officefloor.eclipse.skin.office.DutyFigureContext)
	 */
	@Override
	public DutyFigure createDutyFigure(DutyFigureContext context) {
		return new StandardDutyFigure(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.skin.office.OfficeFigureFactory#createDutyFlowFigure
	 * (net.officefloor.eclipse.skin.office.DutyFlowFigureContext)
	 */
	@Override
	public DutyFlowFigure createDutyFlowFigure(DutyFlowFigureContext context) {
		return new StandardDutyFlowFigure(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.skin.office.OfficeFigureFactory#
	 * createExternalManagedObjectFigure
	 * (net.officefloor.eclipse.skin.office.ExternalManagedObjectFigureContext)
	 */
	@Override
	public ExternalManagedObjectFigure createExternalManagedObjectFigure(
			final ExternalManagedObjectFigureContext context) {
		return new StandardExternalManagedObjectFigure(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.skin.office.OfficeFigureFactory#
	 * createExternalTeamFigure
	 * (net.officefloor.eclipse.skin.office.ExternalTeamFigureContext)
	 */
	@Override
	public ExternalTeamFigure createExternalTeamFigure(
			ExternalTeamFigureContext context) {
		return new StandardExternalTeamFigure(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.skin.office.OfficeFigureFactory#createFlowItemFigure
	 * (net.officefloor.eclipse.skin.office.FlowItemFigureContext)
	 */
	@Override
	public FlowItemFigure createFlowItemFigure(FlowItemFigureContext context) {
		return new StandardFlowItemFigure(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.skin.office.OfficeFigureFactory#createRoomFigure
	 * (net.officefloor.eclipse.skin.office.RoomFigureContext)
	 */
	@Override
	public RoomFigure createRoomFigure(RoomFigureContext context) {
		return new StandardRoomFigure(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.skin.office.OfficeFigureFactory#
	 * createFlowItemAdministrationJoinPointFigure()
	 */
	@Override
	public FlowItemAdministrationJoinPointFigure createFlowItemAdministrationJoinPointFigure() {
		return new StandardFlowItemAdministrationJoinPointFigure();
	}

}
