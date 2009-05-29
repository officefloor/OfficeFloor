/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.eclipse.skin.standard.section;

import net.officefloor.eclipse.skin.section.SubSectionOutputFigure;
import net.officefloor.eclipse.skin.section.SubSectionOutputFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.figure.SubSectionItemFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.section.SubSectionOutputToExternalFlowModel;
import net.officefloor.model.section.SubSectionOutputToSubSectionInputModel;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;

/**
 * Standard {@link SubSectionOutputFigure}.
 * 
 * @author Daniel Sagenschneider
 */
public class StandardSubSectionOutputFigure extends AbstractOfficeFloorFigure
		implements SubSectionOutputFigure {

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link SubSectionOutputFigureContext}.
	 */
	public StandardSubSectionOutputFigure(
			SubSectionOutputFigureContext context) {
		SubSectionItemFigure figure = new SubSectionItemFigure(context
				.getSubSectionOutputName(), false, ConnectorDirection.EAST,
				ColorConstants.black);
		ConnectionAnchor anchor = figure.getConnectionAnchor();
		this.registerConnectionAnchor(SubSectionOutputToExternalFlowModel.class,
				anchor);
		this.registerConnectionAnchor(SubSectionOutputToSubSectionInputModel.class, anchor);
		this.setFigure(figure);
	}
}