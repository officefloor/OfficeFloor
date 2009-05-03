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
package net.officefloor.eclipse.skin.standard.officefloor;

import net.officefloor.eclipse.skin.officefloor.ManagedObjectDependencyFigure;
import net.officefloor.eclipse.skin.officefloor.ManagedObjectDependencyFigureContext;
import net.officefloor.eclipse.skin.officefloor.ManagedObjectSourceFigure;
import net.officefloor.eclipse.skin.officefloor.ManagedObjectSourceFigureContext;
import net.officefloor.eclipse.skin.officefloor.ManagedObjectTaskFlowFigure;
import net.officefloor.eclipse.skin.officefloor.ManagedObjectTaskFlowFigureContext;
import net.officefloor.eclipse.skin.officefloor.ManagedObjectTeamFigure;
import net.officefloor.eclipse.skin.officefloor.ManagedObjectTeamFigureContext;
import net.officefloor.eclipse.skin.officefloor.OfficeFigure;
import net.officefloor.eclipse.skin.officefloor.OfficeFigureContext;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorFigureFactory;
import net.officefloor.eclipse.skin.officefloor.OfficeManagedObjectFigure;
import net.officefloor.eclipse.skin.officefloor.OfficeManagedObjectFigureContext;
import net.officefloor.eclipse.skin.officefloor.OfficeTaskFigureContext;
import net.officefloor.eclipse.skin.officefloor.OfficeTeamFigure;
import net.officefloor.eclipse.skin.officefloor.OfficeTeamFigureContext;
import net.officefloor.eclipse.skin.officefloor.TeamFigure;
import net.officefloor.eclipse.skin.officefloor.TeamFigureContext;

/**
 * Standard {@link OfficeFloorFigureFactory}.
 * 
 * @author Daniel
 */
public class StandardOfficeFloorFigureFactory implements
		OfficeFloorFigureFactory {

	@Override
	public ManagedObjectDependencyFigure createManagedObjectDependencyFigure(
			ManagedObjectDependencyFigureContext context) {
		return new StandardManagedObjectDependencyFigure(context);
	}

	@Override
	public ManagedObjectSourceFigure createManagedObjectSourceFigure(
			ManagedObjectSourceFigureContext context) {
		return new StandardManagedObjectSourceFigure(context);
	}

	@Override
	public ManagedObjectTaskFlowFigure createManagedObjectTaskFlowFigure(
			ManagedObjectTaskFlowFigureContext context) {
		return new StandardManagedObjectTaskFlowFigure(context);
	}

	@Override
	public ManagedObjectTeamFigure createManagedObjectTeamFigure(
			ManagedObjectTeamFigureContext context) {
		return new StandardManagedObjectTeamFigure(context);
	}

	@Override
	public OfficeFigure createOfficeFigure(OfficeFigureContext context) {
		return new StandardOfficeFigure(context);
	}

	@Override
	public OfficeManagedObjectFigure createOfficeManagedObject(
			OfficeManagedObjectFigureContext context) {
		return new StandardOfficeManagedObjectFigure(context);
	}

	@Override
	public net.officefloor.eclipse.skin.officefloor.OfficeTaskFigure createOfficeTaskFigure(
			OfficeTaskFigureContext context) {
		return new StandardOfficeTaskFigure(context);
	}

	@Override
	public OfficeTeamFigure createOfficeTeamFigure(
			OfficeTeamFigureContext context) {
		return new StandardOfficeTeamFigure(context);
	}

	@Override
	public TeamFigure createTeamFigure(TeamFigureContext context) {
		return new StandardTeamFigure(context);
	}

}