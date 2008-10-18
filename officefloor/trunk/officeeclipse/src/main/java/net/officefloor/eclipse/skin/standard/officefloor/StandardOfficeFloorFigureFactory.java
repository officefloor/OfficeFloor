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
import net.officefloor.eclipse.skin.officefloor.ManagedObjectSourceFigureContext;
import net.officefloor.eclipse.skin.officefloor.ManagedObjectTaskFigureContext;
import net.officefloor.eclipse.skin.officefloor.ManagedObjectTaskFlowFigureContext;
import net.officefloor.eclipse.skin.officefloor.ManagedObjectTeamFigureContext;
import net.officefloor.eclipse.skin.officefloor.OfficeFigureContext;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorFigureFactory;
import net.officefloor.eclipse.skin.officefloor.OfficeManagedObjectFigureContext;
import net.officefloor.eclipse.skin.officefloor.OfficeTaskFigureContext;
import net.officefloor.eclipse.skin.officefloor.OfficeTeamFigureContext;
import net.officefloor.eclipse.skin.officefloor.TeamFigureContext;
import net.officefloor.eclipse.skin.standard.OfficeFloorFigureImpl;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FlowLayout;
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
	public OfficeFloorFigure createManagedObjectSourceFigure(
			ManagedObjectSourceFigureContext context) {
		WrappingFigure figure = new WrappingFigure(new IndentFigure(5,
				new ListFigure()));
		figure.addDecorate(new ListItemFigure(context
				.getManagedObjectSourceName()));
		figure.addChildContainerFigure();
		figure.setBackgroundColor(ColorConstants.lightGray);
		figure.setOpaque(true);
		return new OfficeFloorFigureImpl(figure);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.skin.officefloor.OfficeFloorFigureFactory#
	 * createManagedObjectTaskFigure
	 * (net.officefloor.eclipse.skin.officefloor.ManagedObjectTaskFigureContext)
	 */
	@Override
	public OfficeFloorFigure createManagedObjectTaskFigure(
			ManagedObjectTaskFigureContext context) {
		// Create the figure
		WrappingFigure figure = new WrappingFigure(new IndentFigure(5,
				new ListFigure()));
		figure.addDecorate(new ListItemFigure(context.getWorkName()
				+ "."
				+ context.getTaskName()
				+ (context.getTeamName() == null ? "" : " ("
						+ context.getTeamName() + ")")));
		figure.addChildContainerFigure();
		figure.setForegroundColor(ColorConstants.darkGreen);

		// Return the figure
		return new OfficeFloorFigureImpl(figure);
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
	public OfficeFloorFigure createManagedObjectTaskFlowFigure(
			ManagedObjectTaskFlowFigureContext context) {

		// Determine if linked by managed object source to task
		String linkTask = "";
		String taskName = context.getInitialTaskName();
		if ((taskName != null) && (taskName.length() > 0)) {
			// Linked to a task by managed object source
			linkTask = " (" + context.getInitialWorkName() + "." + taskName
					+ ")";
		}

		// Create the figure
		IFigure figure = new Label(context.getFlowName() + linkTask);
		figure.setForegroundColor(ColorConstants.cyan);

		// Return the figure
		return new OfficeFloorFigureImpl(figure);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.skin.officefloor.OfficeFloorFigureFactory#
	 * createManagedObjectTeamFigure
	 * (net.officefloor.eclipse.skin.officefloor.ManagedObjectTeamFigureContext)
	 */
	@Override
	public OfficeFloorFigure createManagedObjectTeamFigure(
			ManagedObjectTeamFigureContext context) {
		// Create the figure
		IFigure figure = new Label(context.getTeamName());
		figure.setForegroundColor(ColorConstants.blue);

		// Return the figure
		return new OfficeFloorFigureImpl(figure);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.skin.officefloor.OfficeFloorFigureFactory#
	 * createOfficeFigure
	 * (net.officefloor.eclipse.skin.officefloor.OfficeFigureContext)
	 */
	@Override
	public OfficeFloorFigure createOfficeFigure(OfficeFigureContext context) {
		return new OfficeFloorFigureImpl(new OfficeFigure(context
				.getOfficeName()));
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
	public OfficeFloorFigure createOfficeManagedObject(
			OfficeManagedObjectFigureContext context) {
		return new OfficeFloorFigureImpl(new OfficeManagedObjectFigure(context
				.getOfficeManagedObjectName()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.skin.officefloor.OfficeFloorFigureFactory#
	 * createOfficeTaskFigure
	 * (net.officefloor.eclipse.skin.officefloor.OfficeTaskFigureContext)
	 */
	@Override
	public OfficeFloorFigure createOfficeTaskFigure(
			OfficeTaskFigureContext context) {
		return new OfficeFloorFigureImpl(new OfficeTaskFigure(context
				.getWorkName()
				+ "." + context.getTaskName()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.skin.officefloor.OfficeFloorFigureFactory#
	 * createOfficeTeamFigure
	 * (net.officefloor.eclipse.skin.officefloor.OfficeTeamFigureContext)
	 */
	@Override
	public OfficeFloorFigure createOfficeTeamFigure(
			OfficeTeamFigureContext context) {
		return new OfficeFloorFigureImpl(new OfficeTeamFigure(context
				.getOfficeTeamName()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.skin.officefloor.OfficeFloorFigureFactory#
	 * createTeamFigure
	 * (net.officefloor.eclipse.skin.officefloor.TeamFigureContext)
	 */
	@Override
	public OfficeFloorFigure createTeamFigure(TeamFigureContext context) {
		Label figure = new Label(context.getTeamName());
		figure.setBackgroundColor(ColorConstants.lightGray);
		figure.setOpaque(true);
		figure.setLayoutManager(new FlowLayout(true));

		// Return figure
		return new OfficeFloorFigureImpl(figure);
	}

}
