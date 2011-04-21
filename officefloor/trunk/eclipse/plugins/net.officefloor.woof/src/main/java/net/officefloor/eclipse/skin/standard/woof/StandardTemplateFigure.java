/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
import net.officefloor.eclipse.skin.standard.StandardWoofColours;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingGridLayout;
import net.officefloor.eclipse.skin.standard.figure.RectangleContainerFigure;
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
import org.eclipse.swt.SWT;

/**
 * Standard {@link TemplateFigure}.
 * 
 * @author Daniel Sagenschneider
 */
public class StandardTemplateFigure extends AbstractOfficeFloorFigure implements
		TemplateFigure {

	/**
	 * {@link TemplateFigureContext}.
	 */
	private final TemplateFigureContext context;

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
		this.context = context;

		// Create the figure
		Figure figure = new Figure();
		NoSpacingGridLayout layout = new NoSpacingGridLayout(2);
		figure.setLayoutManager(layout);

		// Link to template
		ConnectorFigure templateInput = new ConnectorFigure(
				ConnectorDirection.WEST, StandardWoofColours.CONNECTOR());
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

		// Create the container for the template
		RectangleContainerFigure templateFigure = new RectangleContainerFigure(
				context.getTemplateName(), StandardWoofColours.TEMPLATE(), 5,
				false);
		figure.add(templateFigure);

		// Specify the template name figure
		this.templateNameFigure = templateFigure.getContainerName();

		// Specify the figures
		this.setFigure(figure);
		this.setContentPane(templateFigure.getContentPane());
	}

	/*
	 * ========================== TemplateFigure =============================
	 */

	@Override
	public void setUri(String uri) {

		// Determine if URI
		String templateName = this.context.getTemplateName();
		boolean isUri = (templateName.equals(this.context.getUri()));

		// Reflect the name on URI change
		this.templateNameFigure.setText((isUri ? "" : "[") + templateName
				+ (isUri ? "" : "]"));
	}

	@Override
	public IFigure getUriFigure() {
		return this.templateNameFigure;
	}

}