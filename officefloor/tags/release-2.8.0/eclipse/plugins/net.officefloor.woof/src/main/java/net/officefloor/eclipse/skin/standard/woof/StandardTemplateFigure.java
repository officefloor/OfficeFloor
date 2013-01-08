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
package net.officefloor.eclipse.skin.standard.woof;

import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingGridLayout;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingToolbarLayout;
import net.officefloor.eclipse.skin.woof.TemplateFigure;
import net.officefloor.eclipse.skin.woof.TemplateFigureContext;
import net.officefloor.model.woof.WoofExceptionToWoofTemplateModel;
import net.officefloor.model.woof.WoofSectionOutputToWoofTemplateModel;
import net.officefloor.model.woof.WoofTemplateOutputToWoofTemplateModel;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

/**
 * Standard {@link TemplateFigure}.
 * 
 * @author Daniel Sagenschneider
 */
public class StandardTemplateFigure extends AbstractOfficeFloorFigure implements
		TemplateFigure {

	/**
	 * {@link IFigure} for the template name.
	 */
	private final Label templateNameFigure;

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link TemplateFigureContext}.
	 */
	public StandardTemplateFigure(TemplateFigureContext context) {

		// Colours
		final Color titleBarTextColor = new Color(null, 0, 0, 0);
		final Color titleBarTopColour = new Color(null, 203, 235, 255);
		final Color titleBarBottomColour = new Color(null, 0, 139, 255);
		final Color windowColour = new Color(null, 229, 229, 229);
		final Color contentColour = new Color(null, 240, 249, 255);

		// Create the figure
		Figure figure = new Figure();
		NoSpacingGridLayout layout = new NoSpacingGridLayout(2);
		figure.setLayoutManager(layout);

		// Link to template
		ConnectorFigure templateInput = new ConnectorFigure(
				ConnectorDirection.WEST, CommonWoofColours.CONNECTIONS());
		templateInput.setBorder(new MarginBorder(10, 0, 0, 0));
		ConnectionAnchor templateInputAnchor = templateInput
				.getConnectionAnchor();
		this.registerConnectionAnchor(
				WoofTemplateOutputToWoofTemplateModel.class,
				templateInputAnchor);
		this.registerConnectionAnchor(
				WoofSectionOutputToWoofTemplateModel.class, templateInputAnchor);
		this.registerConnectionAnchor(WoofExceptionToWoofTemplateModel.class,
				templateInputAnchor);
		layout.setConstraint(templateInput, new GridData(SWT.BEGINNING,
				SWT.BEGINNING, false, false));
		figure.add(templateInput);

		// Create the rounded rectangle container
		final RoundedRectangle window = new RoundedRectangle();
		NoSpacingGridLayout windowLayout = new NoSpacingGridLayout(1);
		window.setLayoutManager(windowLayout);
		window.setBackgroundColor(windowColour);
		window.setOutline(false);
		figure.add(window);

		// Create the title bar for URI and security
		TitleBarFigure titleBar = new TitleBarFigure(
				context.getTemplateDisplayName(), titleBarTextColor,
				titleBarTopColour, titleBarBottomColour);
		this.templateNameFigure = titleBar.getTitleNameFigure();
		window.add(titleBar);
		windowLayout.setConstraint(titleBar, new GridData(SWT.FILL, 0, true,
				false));

		// Provide window border to content
		Figure contentPaneWrap = new Figure();
		NoSpacingGridLayout contentPaneWrapLayout = new NoSpacingGridLayout(1);
		contentPaneWrap.setLayoutManager(contentPaneWrapLayout);
		contentPaneWrap.setBorder(new MarginBorder(4, 4, 4, 4));
		window.add(contentPaneWrap);
		windowLayout.setConstraint(contentPaneWrap, new GridData(SWT.FILL, 0,
				true, false));

		// Add content pane
		RectangleFigure contentPane = new RectangleFigure();
		contentPane.setLayoutManager(new NoSpacingToolbarLayout(false));
		contentPane.setBorder(new MarginBorder(2, 2, 2, 2));
		contentPane.setBackgroundColor(contentColour);
		contentPane.setOutline(false);
		contentPaneWrap.add(contentPane);
		contentPaneWrapLayout.setConstraint(contentPane, new GridData(SWT.FILL,
				0, true, false));

		// Specify the figures
		this.setFigure(figure);
		this.setContentPane(contentPane);
	}

	/*
	 * ========================== TemplateFigure =============================
	 */

	@Override
	public void setTemplateDisplayName(String templateDisplayName) {
		this.templateNameFigure.setText(templateDisplayName);
	}

	@Override
	public IFigure getTemplateDisplayFigure() {
		return this.templateNameFigure;
	}

}