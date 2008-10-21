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
package net.officefloor.eclipse.skin.standard.desk;

import net.officefloor.eclipse.skin.desk.FlowItemFigure;
import net.officefloor.eclipse.skin.desk.FlowItemFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingGridLayout;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.desk.DeskTaskToFlowItemModel;
import net.officefloor.model.desk.DeskWorkToFlowItemModel;
import net.officefloor.model.desk.FlowItemEscalationToFlowItemModel;
import net.officefloor.model.desk.FlowItemOutputToFlowItemModel;
import net.officefloor.model.desk.FlowItemToNextExternalFlowModel;
import net.officefloor.model.desk.FlowItemToNextFlowItemModel;

import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

/**
 * {@link StandardFlowItemFigure} implementation.
 * 
 * @author Daniel
 */
public class StandardFlowItemFigure extends AbstractOfficeFloorFigure implements
		FlowItemFigure {

	/**
	 * Is public {@link Figure}.
	 */
	private final Ellipse isPublic;

	/**
	 * Initiate.
	 */
	public StandardFlowItemFigure(final FlowItemFigureContext context) {

		Color flowColour = new Color(null, 130, 255, 150);

		// Create the figure
		Figure figure = new Figure();
		NoSpacingGridLayout layout = new NoSpacingGridLayout(3);
		figure.setLayoutManager(layout);

		// Create the connector
		ConnectorFigure inputConnector = new ConnectorFigure(
				ConnectorDirection.WEST, ColorConstants.black);
		inputConnector.setBorder(new MarginBorder(10, 0, 0, 0));
		ConnectionAnchor inputAnchor = inputConnector.getConnectionAnchor();
		this.registerConnectionAnchor(FlowItemOutputToFlowItemModel.class,
				inputAnchor);
		this.registerConnectionAnchor(FlowItemEscalationToFlowItemModel.class,
				inputAnchor);
		this.registerConnectionAnchor(DeskWorkToFlowItemModel.class,
				inputAnchor);
		this.registerTargetConnectionAnchor(FlowItemToNextFlowItemModel.class,
				inputAnchor);
		figure.add(inputConnector);
		layout.setConstraint(inputConnector, new GridData(SWT.BEGINNING,
				SWT.BEGINNING, true, false));

		// Create container of flow item and next flow connector
		Figure flowItemAndTaskLink = new Figure();
		NoSpacingGridLayout flowItemAndNextFlowLayout = new NoSpacingGridLayout(
				1);
		flowItemAndTaskLink.setLayoutManager(flowItemAndNextFlowLayout);
		figure.add(flowItemAndTaskLink);

		// Create the flow item container
		RoundedRectangle flowItem = new RoundedRectangle();
		NoSpacingGridLayout flowItemLayout = new NoSpacingGridLayout(1);
		flowItem.setLayoutManager(flowItemLayout);
		flowItem.setBackgroundColor(flowColour);
		flowItem.setOpaque(true);
		flowItemAndTaskLink.add(flowItem);

		// Create the header
		Figure header = new Figure();
		GridLayout headerLayout = new GridLayout(2, false);
		header.setLayoutManager(headerLayout);
		flowItem.add(header);

		// Is public figure
		this.isPublic = new Ellipse();
		this.isPublic.setSize(6, 6);
		this.isPublic.setBackgroundColor(ColorConstants.black);
		header.add(this.isPublic);

		// Initiate state of is public
		this.setIsPublic(context.isPublic());

		// Specify the flow item name
		Label flowItemName = new Label(context.getFlowItemName());
		header.add(flowItemName);

		// Content pane
		Figure contentPaneWrap = new Figure();
		contentPaneWrap.setLayoutManager(new ToolbarLayout());
		contentPaneWrap.setBorder(new ContentBorder());
		Figure contentPane = new Figure();
		ToolbarLayout contentLayout = new ToolbarLayout(false);
		contentLayout.setSpacing(5);
		contentPane.setLayoutManager(contentLayout);
		contentPane.setBorder(new MarginBorder(2, 20, 2, 2));
		contentPaneWrap.add(contentPane);
		flowItem.add(contentPaneWrap);
		flowItemLayout.setConstraint(contentPaneWrap, new GridData(SWT.FILL, 0,
				true, false));

		// Add the connector for task
		ConnectorFigure taskConnector = new ConnectorFigure(
				ConnectorDirection.SOUTH, ColorConstants.lightGray);
		taskConnector.setBorder(new MarginBorder(0, 20, 0, 0));
		this.registerConnectionAnchor(DeskTaskToFlowItemModel.class,
				taskConnector.getConnectionAnchor());
		flowItemAndTaskLink.add(taskConnector);

		// Add next flow connector
		ConnectorFigure nextFlow = new ConnectorFigure(ConnectorDirection.EAST,
				ColorConstants.black);
		nextFlow.setBorder(new MarginBorder(10, 0, 0, 0));
		ConnectionAnchor nextFlowAnchor = nextFlow.getConnectionAnchor();
		this.registerSourceConnectionAnchor(FlowItemToNextFlowItemModel.class,
				nextFlowAnchor);
		this.registerConnectionAnchor(FlowItemToNextExternalFlowModel.class,
				nextFlowAnchor);
		layout.setConstraint(nextFlow, new GridData(SWT.BEGINNING,
				SWT.BEGINNING, true, false));
		figure.add(nextFlow);

		// Specify figures
		this.setFigure(figure);
		this.setContentPane(contentPane);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.skin.desk.FlowItemFigure#setIsPublic(boolean)
	 */
	@Override
	public void setIsPublic(boolean isPublic) {
		this.isPublic.setVisible(isPublic);
	}

	/**
	 * {@link Border} for the content.
	 */
	private class ContentBorder extends AbstractBorder {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.draw2d.Border#getInsets(org.eclipse.draw2d.IFigure)
		 */
		@Override
		public Insets getInsets(IFigure figure) {
			return new Insets(0);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.draw2d.Border#paint(org.eclipse.draw2d.IFigure,
		 * org.eclipse.draw2d.Graphics, org.eclipse.draw2d.geometry.Insets)
		 */
		@Override
		public void paint(IFigure figure, Graphics graphics, Insets insets) {
			Rectangle paintRectangle = getPaintRectangle(figure, insets);
			graphics.drawLine(paintRectangle.getTopLeft(), paintRectangle
					.getTopRight());
		}
	}

}
