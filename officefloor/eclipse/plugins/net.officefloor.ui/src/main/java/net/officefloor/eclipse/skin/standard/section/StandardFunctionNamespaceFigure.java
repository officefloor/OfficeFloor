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
package net.officefloor.eclipse.skin.standard.section;

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

import net.officefloor.eclipse.skin.section.FunctionNamespaceFigure;
import net.officefloor.eclipse.skin.section.FunctionNamespaceFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingGridLayout;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingToolbarLayout;
import net.officefloor.model.section.FunctionNamespaceModel;

/**
 * Standard {@link FunctionNamespaceFigure}.
 *
 * @author Daniel Sagenschneider
 */
public class StandardFunctionNamespaceFigure extends AbstractOfficeFloorFigure implements FunctionNamespaceFigure {

	/**
	 * {@link Label} containing the {@link FunctionNamespaceModel} name.
	 */
	private final Label namespaceName;

	/**
	 * Initiate.
	 *
	 * @param context
	 *            {@link FunctionNamespaceFigureContext}.
	 */
	public StandardFunctionNamespaceFigure(FunctionNamespaceFigureContext context) {

		// Create the figure
		Figure figure = new Figure();
		NoSpacingGridLayout layout = new NoSpacingGridLayout(2);
		figure.setLayoutManager(layout);

		// Create the namespace figure
		Figure namespaceFigure = new Figure();
		namespaceFigure.setBorder(new LineBorder(1));
		NoSpacingGridLayout workLayout = new NoSpacingGridLayout(1);
		namespaceFigure.setLayoutManager(workLayout);
		figure.add(namespaceFigure);

		// Name of namespace
		this.namespaceName = new Label(context.getFunctionNamespaceName());
		this.namespaceName.setBorder(new MarginBorder(2, 5, 2, 2));
		namespaceFigure.add(this.namespaceName);

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
		namespaceFigure.add(contentPaneWrap);
		workLayout.setConstraint(contentPaneWrap, new GridData(SWT.FILL, SWT.BEGINNING, true, false));

		// Specify figures, content pane
		this.setFigure(figure);
		this.setContentPane(contentPane);
	}

	/*
	 * ==================== FunctionNamespaceFigure ====================
	 */

	@Override
	public void setFunctionNamespaceName(String workName) {
		this.namespaceName.setText(workName);
	}

	@Override
	public IFigure getFunctionNamespaceNameFigure() {
		return this.namespaceName;
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
			graphics.drawLine(paintRectangle.getTopLeft(), paintRectangle.getTopRight());
		}
	}

}