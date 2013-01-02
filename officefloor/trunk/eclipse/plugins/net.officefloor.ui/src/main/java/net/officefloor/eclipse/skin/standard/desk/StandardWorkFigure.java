/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.eclipse.skin.standard.desk;

import net.officefloor.eclipse.skin.desk.WorkFigure;
import net.officefloor.eclipse.skin.desk.WorkFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingGridLayout;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingToolbarLayout;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.model.desk.WorkToInitialTaskModel;

import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

/**
 * Standard {@link WorkFigure}.
 *
 * @author Daniel Sagenschneider
 */
public class StandardWorkFigure extends AbstractOfficeFloorFigure implements
		WorkFigure {

	/**
	 * {@link Label} containing the {@link Work} name.
	 */
	private final Label workName;

	/**
	 * Initiate.
	 *
	 * @param context
	 *            {@link WorkFigureContext}.
	 */
	public StandardWorkFigure(WorkFigureContext context) {

		// Create the figure
		Figure figure = new Figure();
		NoSpacingGridLayout layout = new NoSpacingGridLayout(2);
		figure.setLayoutManager(layout);

		// Create the work figure
		Figure workFigure = new Figure();
		workFigure.setBorder(new LineBorder(1));
		NoSpacingGridLayout workLayout = new NoSpacingGridLayout(1);
		workFigure.setLayoutManager(workLayout);
		figure.add(workFigure);

		// Name of work
		this.workName = new Label(context.getWorkName());
		this.workName.setBorder(new MarginBorder(2, 5, 2, 2));
		workFigure.add(this.workName);

		// Content pane
		Figure contentPaneWrap = new Figure();
		contentPaneWrap.setLayoutManager(new NoSpacingToolbarLayout(false));
		contentPaneWrap.setBorder(new ContentBorder());
		Figure contentPane = new Figure();
		ToolbarLayout contentLayout = new ToolbarLayout(false);
		contentLayout.setSpacing(5);
		contentPane.setLayoutManager(contentLayout);
		contentPane.setBorder(new MarginBorder(2, 5, 2, 2));
		contentPaneWrap.add(contentPane);
		workFigure.add(contentPaneWrap);
		workLayout.setConstraint(contentPaneWrap, new GridData(SWT.FILL,
				SWT.BEGINNING, true, false));

		// Link to initial task
		ConnectorFigure initialFlowConnector = new ConnectorFigure(
				ConnectorDirection.EAST, StandardOfficeFloorColours
						.INITIAL_TASK_LINE());
		figure.add(initialFlowConnector);
		this.registerConnectionAnchor(WorkToInitialTaskModel.class,
				initialFlowConnector.getConnectionAnchor());
		layout.setConstraint(initialFlowConnector, new GridData(SWT.BEGINNING,
				SWT.BEGINNING, false, false));

		// Specify figures, content pane
		this.setFigure(figure);
		this.setContentPane(contentPane);
	}

	/*
	 * ==================== WorkFigure ========================================
	 */

	@Override
	public void setWorkName(String workName) {
		this.workName.setText(workName);
	}

	@Override
	public IFigure getWorkNameFigure() {
		return this.workName;
	}

	/**
	 * {@link Border} for the content.
	 */
	private class ContentBorder extends AbstractBorder {

		/*
		 * =============== Border ========================================
		 */

		@Override
		public Insets getInsets(IFigure figure) {
			return new Insets(0);
		}

		@Override
		public void paint(IFigure figure, Graphics graphics, Insets insets) {
			Rectangle paintRectangle = getPaintRectangle(figure, insets);
			graphics.drawLine(paintRectangle.getTopLeft(), paintRectangle
					.getTopRight());
		}
	}
}
