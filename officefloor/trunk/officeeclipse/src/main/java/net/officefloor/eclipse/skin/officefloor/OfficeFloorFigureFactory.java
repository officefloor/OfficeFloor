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
package net.officefloor.eclipse.skin.officefloor;

import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.officefloor.ManagedObjectDependencyModel;
import net.officefloor.model.officefloor.ManagedObjectHandlerInstanceModel;
import net.officefloor.model.officefloor.ManagedObjectHandlerModel;
import net.officefloor.model.officefloor.ManagedObjectSourceModel;
import net.officefloor.model.officefloor.ManagedObjectTaskFlowModel;
import net.officefloor.model.officefloor.ManagedObjectTaskModel;
import net.officefloor.model.officefloor.ManagedObjectTeamModel;
import net.officefloor.model.officefloor.OfficeFloorModel;
import net.officefloor.model.officefloor.OfficeManagedObjectModel;
import net.officefloor.model.officefloor.OfficeTaskModel;
import net.officefloor.model.officefloor.TeamModel;
import net.officefloor.model.officefloor.ManagedObjectHandlerLinkProcessModel.ManagedObjectHandlerLinkProcessEvent;

import org.eclipse.draw2d.IFigure;

/**
 * Factory to create the {@link IFigure} instances for the skin of the
 * {@link OfficeFloorModel}.
 * 
 * @author Daniel
 */
public interface OfficeFloorFigureFactory {

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link ManagedObjectDependencyModel}.
	 * 
	 * @param context
	 *            {@link ManagedObjectDependencyFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeFloorFigure createManagedObjectDependencyFigure(
			ManagedObjectDependencyFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link ManagedObjectHandlerModel}.
	 * 
	 * @param context
	 *            {@link ManagedObjectHandlerFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeFloorFigure createManagedObjectHandlerFigure(
			ManagedObjectHandlerFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link ManagedObjectHandlerInstanceModel}.
	 * 
	 * @param context
	 *            {@link ManagedObjectHandlerFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeFloorFigure createManagedObjectHandlerInstanceFigure(
			ManagedObjectHandlerInstanceFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link ManagedObjectHandlerLinkProcessEvent}.
	 * 
	 * @param context
	 *            {@link ManagedObjectHandlerLinkProcessFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeFloorFigure createManagedObjectHandlerLinkProcessFigure(
			ManagedObjectHandlerLinkProcessFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link ManagedObjectSourceModel}.
	 * 
	 * @param context
	 *            {@link ManagedObjectSourceFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeFloorFigure createManagedObjectSourceFigure(
			ManagedObjectSourceFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link ManagedObjectTaskModel}.
	 * 
	 * @param context
	 *            {@link ManagedObjectTaskFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeFloorFigure createManagedObjectTaskFigure(
			ManagedObjectTaskFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link ManagedObjectTaskFlowModel}.
	 * 
	 * @param context
	 *            {@link ManagedObjectTaskFlowFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeFloorFigure createManagedObjectTaskFlowFigure(
			ManagedObjectTaskFlowFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link ManagedObjectTeamModel}.
	 * 
	 * @param context
	 *            {@link ManagedObjectTeamFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeFloorFigure createManagedObjectTeamFigure(
			ManagedObjectTeamFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link OfficeModel}.
	 * 
	 * @param context
	 *            {@link OfficeFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeFloorFigure createOfficeFigure(OfficeFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the
	 * {@link OfficeManagedObjectModel}.
	 * 
	 * @param context
	 *            {@link OfficeManagedObjectFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeFloorFigure createOfficeManagedObject(
			OfficeManagedObjectFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link OfficeTaskModel}.
	 * 
	 * @param context
	 *            {@link OfficeTaskFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeFloorFigure createOfficeTaskFigure(OfficeTaskFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link }.
	 * 
	 * @param context
	 *            {@link }.
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeFloorFigure createOfficeTeamFigure(OfficeTeamFigureContext context);

	/**
	 * Creates the {@link OfficeFloorFigure} for the {@link TeamModel}.
	 * 
	 * @param context
	 *            {@link TeamFigureContext}.
	 * @return {@link OfficeFloorFigure}.
	 */
	OfficeFloorFigure createTeamFigure(TeamFigureContext context);

}
