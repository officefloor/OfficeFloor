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

import net.officefloor.eclipse.common.figure.IndentFigure;
import net.officefloor.eclipse.common.figure.ListFigure;
import net.officefloor.eclipse.common.figure.ListItemFigure;
import net.officefloor.eclipse.common.figure.WrappingFigure;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.officefloor.ManagedObjectDependencyFigureContext;
import net.officefloor.eclipse.skin.officefloor.ManagedObjectHandlerFigureContext;
import net.officefloor.eclipse.skin.officefloor.ManagedObjectHandlerInstanceFigureContext;
import net.officefloor.eclipse.skin.officefloor.ManagedObjectHandlerLinkProcessFigureContext;
import net.officefloor.eclipse.skin.officefloor.ManagedObjectSourceFigure;
import net.officefloor.eclipse.skin.officefloor.ManagedObjectSourceFigureContext;
import net.officefloor.eclipse.skin.officefloor.ManagedObjectTaskFigure;
import net.officefloor.eclipse.skin.officefloor.ManagedObjectTaskFigureContext;
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
import net.officefloor.eclipse.skin.standard.OfficeFloorFigureImpl;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;

/**
 * Standard {@link OfficeFloorFigureFactory}.
 * 
 * @author Daniel
 */
public class StandardOfficeFloorFigureFactory implements
		OfficeFloorFigureFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.skin.officefloor.OfficeFloorFigureFactory#
	 * createManagedObjectDependencyFigure
	 * (net.officefloor.eclipse.skin.officefloor
	 * .ManagedObjectDependencyFigureContext)
	 */
	@Override
	public OfficeFloorFigure createManagedObjectDependencyFigure(
			ManagedObjectDependencyFigureContext context) {
		// Create the figure
		IFigure figure = new Label(context.getManagedObjectDependencyName());
		figure.setForegroundColor(ColorConstants.white);

		// Return the figure
		return new OfficeFloorFigureImpl(figure);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.skin.officefloor.OfficeFloorFigureFactory#
	 * createManagedObjectHandlerFigure
	 * (net.officefloor.eclipse.skin.officefloor.
	 * ManagedObjectHandlerFigureContext)
	 */
	@Override
	public OfficeFloorFigure createManagedObjectHandlerFigure(
			ManagedObjectHandlerFigureContext context) {
		WrappingFigure figure = new WrappingFigure(new IndentFigure(5,
				new ListFigure()));
		figure.addDecorate(new ListItemFigure(context
				.getManagedObjectHandlerName()));
		figure.addChildContainerFigure();
		figure.setForegroundColor(ColorConstants.yellow);
		return new OfficeFloorFigureImpl(figure);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.skin.officefloor.OfficeFloorFigureFactory#
	 * createManagedObjectHandlerInstanceFigure
	 * (net.officefloor.eclipse.skin.officefloor
	 * .ManagedObjectHandlerInstanceFigureContext)
	 */
	@Override
	public OfficeFloorFigure createManagedObjectHandlerInstanceFigure(
			ManagedObjectHandlerInstanceFigureContext context) {
		WrappingFigure figure = new WrappingFigure(new IndentFigure(5,
				new ListFigure()));
		figure.addDecorate(new ListItemFigure("Instance"));
		figure.addChildContainerFigure();
		figure.setForegroundColor(ColorConstants.black);
		return new OfficeFloorFigureImpl(figure);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.skin.officefloor.OfficeFloorFigureFactory#
	 * createManagedObjectHandlerLinkProcessFigure
	 * (net.officefloor.eclipse.skin.officefloor
	 * .ManagedObjectHandlerLinkProcessFigureContext)
	 */
	@Override
	public OfficeFloorFigure createManagedObjectHandlerLinkProcessFigure(
			ManagedObjectHandlerLinkProcessFigureContext context) {

		// Determine if linked by managed object source to task
		String linkTask = "";
		String taskName = context.getTaskName();
		if ((taskName != null) && (taskName.length() > 0)) {
			// Linked to a task by managed object source
			linkTask = " (" + context.getWorkName() + "." + taskName + ")";
		}

		// Create the figure
		IFigure figure = new Label(context.getLinkProcessName() + linkTask);
		figure.setForegroundColor(ColorConstants.red);

		// Return the figure
		return new OfficeFloorFigureImpl(figure);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.skin.officefloor.OfficeFloorFigureFactory#
	 * createManagedObjectSourceFigure
	 * (net.officefloor.eclipse.skin.officefloor.
	 * ManagedObjectSourceFigureContext )
	 */
	@Override
	public ManagedObjectSourceFigure createManagedObjectSourceFigure(
			ManagedObjectSourceFigureContext context) {
		return new StandardManagedObjectSourceFigure(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.skin.officefloor.OfficeFloorFigureFactory#
	 * createManagedObjectTaskFigure
	 * (net.officefloor.eclipse.skin.officefloor.ManagedObjectTaskFigureContext)
	 */
	@Override
	public ManagedObjectTaskFigure createManagedObjectTaskFigure(
			ManagedObjectTaskFigureContext context) {
		return new StandardManagedObjectTaskFigure(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.skin.officefloor.OfficeFloorFigureFactory#
	 * createManagedObjectTaskFlowFigure
	 * (net.officefloor.eclipse.skin.officefloor
	 * .ManagedObjectTaskFlowFigureContext)
	 */
	@Override
	public ManagedObjectTaskFlowFigure createManagedObjectTaskFlowFigure(
			ManagedObjectTaskFlowFigureContext context) {
		return new StandardManagedObjectTaskFlowFigure(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.skin.officefloor.OfficeFloorFigureFactory#
	 * createManagedObjectTeamFigure
	 * (net.officefloor.eclipse.skin.officefloor.ManagedObjectTeamFigureContext)
	 */
	@Override
	public ManagedObjectTeamFigure createManagedObjectTeamFigure(
			ManagedObjectTeamFigureContext context) {
		return new StandardManagedObjectTeamFigure(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.skin.officefloor.OfficeFloorFigureFactory#
	 * createOfficeFigure
	 * (net.officefloor.eclipse.skin.officefloor.OfficeFigureContext)
	 */
	@Override
	public OfficeFigure createOfficeFigure(OfficeFigureContext context) {
		return new StandardOfficeFigure(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.skin.officefloor.OfficeFloorFigureFactory#
	 * createOfficeManagedObject
	 * (net.officefloor.eclipse.skin.officefloor.OfficeManagedObjectFigureContext
	 * )
	 */
	@Override
	public OfficeManagedObjectFigure createOfficeManagedObject(
			OfficeManagedObjectFigureContext context) {
		return new StandardOfficeManagedObjectFigure(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.skin.officefloor.OfficeFloorFigureFactory#
	 * createOfficeTaskFigure
	 * (net.officefloor.eclipse.skin.officefloor.OfficeTaskFigureContext)
	 */
	@Override
	public net.officefloor.eclipse.skin.officefloor.OfficeTaskFigure createOfficeTaskFigure(
			OfficeTaskFigureContext context) {
		return new StandardOfficeTaskFigure(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.skin.officefloor.OfficeFloorFigureFactory#
	 * createOfficeTeamFigure
	 * (net.officefloor.eclipse.skin.officefloor.OfficeTeamFigureContext)
	 */
	@Override
	public OfficeTeamFigure createOfficeTeamFigure(
			OfficeTeamFigureContext context) {
		return new StandardOfficeTeamFigure(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.skin.officefloor.OfficeFloorFigureFactory#
	 * createTeamFigure
	 * (net.officefloor.eclipse.skin.officefloor.TeamFigureContext)
	 */
	@Override
	public TeamFigure createTeamFigure(TeamFigureContext context) {
		return new StandardTeamFigure(context);
	}

}
