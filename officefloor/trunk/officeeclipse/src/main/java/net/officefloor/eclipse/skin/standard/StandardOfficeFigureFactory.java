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
package net.officefloor.eclipse.skin.standard;

import net.officefloor.eclipse.common.figure.FreeformWrapperFigure;
import net.officefloor.eclipse.common.figure.IndentFigure;
import net.officefloor.eclipse.common.figure.ListFigure;
import net.officefloor.eclipse.common.figure.ListItemFigure;
import net.officefloor.eclipse.common.figure.WrappingFigure;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.office.AdministratorFigureContext;
import net.officefloor.eclipse.skin.office.DeskFigureContext;
import net.officefloor.eclipse.skin.office.DutyFigureContext;
import net.officefloor.eclipse.skin.office.DutyFlowFigureContext;
import net.officefloor.eclipse.skin.office.ExternalManagedObjectFigureContext;
import net.officefloor.eclipse.skin.office.ExternalTeamFigureContext;
import net.officefloor.eclipse.skin.office.FlowItemFigureContext;
import net.officefloor.eclipse.skin.office.OfficeFigureFactory;
import net.officefloor.eclipse.skin.office.RoomFigureContext;

import org.eclipse.draw2d.ActionEvent;
import org.eclipse.draw2d.ActionListener;
import org.eclipse.draw2d.Clickable;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Rectangle;

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
	public OfficeFloorFigure createAdministratorFigure(
			AdministratorFigureContext context) {

		// Create the indent figure to contain children
		IndentFigure indentFigure = new IndentFigure(10, new ListFigure());
		indentFigure.setBackgroundColor(ColorConstants.cyan);

		// Create the figure
		WrappingFigure figure = new WrappingFigure(indentFigure);
		figure.addDecorate(new ListItemFigure(context.getAdministratorName()));
		figure.addChildContainerFigure();
		figure.setBackgroundColor(ColorConstants.cyan);

		// Return the figure for free form display
		FreeformWrapperFigure wrappingFigure = new FreeformWrapperFigure(figure);
		wrappingFigure.setBackgroundColor(ColorConstants.cyan);
		return new OfficeFloorFigure(wrappingFigure);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.skin.office.OfficeFigureFactory#createDeskFigure
	 * (net.officefloor.eclipse.skin.office.DeskFigureContext)
	 */
	@Override
	public OfficeFloorFigure createDeskFigure(DeskFigureContext context) {
		WrappingFigure figure = new WrappingFigure(new IndentFigure(5,
				new ListFigure()));
		figure.addDecorate(new ListItemFigure(context.getDeskName()));
		figure.addChildContainerFigure();
		return new OfficeFloorFigure(figure);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.skin.office.OfficeFigureFactory#createDutyFigure
	 * (net.officefloor.eclipse.skin.office.DutyFigureContext)
	 */
	@Override
	public OfficeFloorFigure createDutyFigure(DutyFigureContext context) {
		// Create the figure
		WrappingFigure figure = new WrappingFigure(new IndentFigure(10,
				new ListFigure()));
		figure.addDecorate(new ListItemFigure(context.getDutyName()));
		figure.addChildContainerFigure();

		// Return the figure
		return new OfficeFloorFigure(figure);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.skin.office.OfficeFigureFactory#createDutyFlowFigure
	 * (net.officefloor.eclipse.skin.office.DutyFlowFigureContext)
	 */
	@Override
	public OfficeFloorFigure createDutyFlowFigure(DutyFlowFigureContext context) {
		return new OfficeFloorFigure(new ListItemFigure(context
				.getDutyFlowName()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.skin.office.OfficeFigureFactory#
	 * createExternalManagedObjectFigure
	 * (net.officefloor.eclipse.skin.office.ExternalManagedObjectFigureContext)
	 */
	@Override
	public OfficeFloorFigure createExternalManagedObjectFigure(
			final ExternalManagedObjectFigureContext context) {

		Figure figure = new Figure();
		figure.setBackgroundColor(ColorConstants.lightGray);
		figure.setOpaque(true);
		figure.setLayoutManager(new FlowLayout(true));

		// Name of external managed object
		Label name = new Label(context.getExternalManagedObjectName());
		figure.add(name);

		// Scope of external managed object
		String scopeName = context.getScope();
		if (scopeName == null) {
			scopeName = "not specified";
		}
		final Label scope = new Label(scopeName);
		scope.setBackgroundColor(ColorConstants.lightBlue);
		scope.setOpaque(true);
		Clickable clickableScope = new Clickable(scope);
		clickableScope.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				// Obtain the next scope
				String nextScope = context.getNextScope(scope.getText());

				// Change scope on model
				context.setScope(nextScope);

				// Change scope on figure
				scope.setText(nextScope);
			}
		});
		figure.add(clickableScope);

		// Return figure
		return new OfficeFloorFigure(figure);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.skin.office.OfficeFigureFactory#
	 * createExternalTeamFigure
	 * (net.officefloor.eclipse.skin.office.ExternalTeamFigureContext)
	 */
	@Override
	public OfficeFloorFigure createExternalTeamFigure(
			ExternalTeamFigureContext context) {

		Label figure = new Label(context.getTeamName());
		figure.setBackgroundColor(ColorConstants.lightGray);
		figure.setOpaque(true);
		figure.setBounds(new Rectangle(140, 30, 120, 20));

		// Return figure
		return new OfficeFloorFigure(figure);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.skin.office.OfficeFigureFactory#createFlowItemFigure
	 * (net.officefloor.eclipse.skin.office.FlowItemFigureContext)
	 */
	@Override
	public OfficeFloorFigure createFlowItemFigure(FlowItemFigureContext context) {

		// Create the figure
		WrappingFigure figure = new WrappingFigure(new ListFigure());
		figure.addDecorate(new ListItemFigure(context.getFlowItemName()));
		figure.setLayoutManager(new ToolbarLayout(true));
		figure.addChildContainerFigure();

		// Return the figure
		return new OfficeFloorFigure(figure);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.skin.office.OfficeFigureFactory#createRoomFigure
	 * (net.officefloor.eclipse.skin.office.RoomFigureContext)
	 */
	@Override
	public OfficeFloorFigure createRoomFigure(RoomFigureContext context) {
		WrappingFigure figure = new WrappingFigure(new IndentFigure(5,
				new ListFigure()));
		figure.addDecorate(new ListItemFigure(context.getRoomName()));
		figure.addChildContainerFigure();
		return new OfficeFloorFigure(figure);
	}

}
