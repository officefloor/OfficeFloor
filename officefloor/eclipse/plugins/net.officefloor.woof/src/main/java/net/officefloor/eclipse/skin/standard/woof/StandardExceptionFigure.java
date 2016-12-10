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
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.eclipse.skin.standard.figure.LabelConnectorFigure;
import net.officefloor.eclipse.skin.woof.ExceptionFigure;
import net.officefloor.eclipse.skin.woof.ExceptionFigureContext;
import net.officefloor.model.woof.WoofExceptionToWoofResourceModel;
import net.officefloor.model.woof.WoofExceptionToWoofSectionInputModel;
import net.officefloor.model.woof.WoofExceptionToWoofTemplateModel;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Label;

/**
 * Standard {@link ExceptionFigure}.
 * 
 * @author Daniel Sagenschneider
 */
public class StandardExceptionFigure extends AbstractOfficeFloorFigure
		implements ExceptionFigure {

	/**
	 * Display name.
	 */
	private final Label name;

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link ExceptionFigureContext}.
	 */
	public StandardExceptionFigure(ExceptionFigureContext context) {

		LabelConnectorFigure figure = new LabelConnectorFigure(
				context.getExceptionName(), ConnectorDirection.EAST,
				CommonWoofColours.CONNECTIONS());
		this.name = figure.getLabel();

		// Register anchors
		ConnectionAnchor anchor = figure.getConnectionAnchor();
		this.registerConnectionAnchor(WoofExceptionToWoofTemplateModel.class,
				anchor);
		this.registerConnectionAnchor(
				WoofExceptionToWoofSectionInputModel.class, anchor);
		this.registerConnectionAnchor(WoofExceptionToWoofResourceModel.class,
				anchor);

		this.setFigure(figure);
	}

	/*
	 * =========================== ExceptionFigure ==========================+
	 */

	@Override
	public void setExceptionName(String exceptionName) {
		this.name.setText(exceptionName);
	}

}