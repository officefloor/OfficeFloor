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
package net.officefloor.eclipse.skin.standard.office;

import net.officefloor.eclipse.skin.office.OfficeSectionInputFigure;
import net.officefloor.eclipse.skin.office.OfficeSectionInputFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.figure.LabelConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.office.OfficeSectionOutputToOfficeSectionInputModel;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;

/**
 * {@link OfficeSectionInputFigure} implementation.
 * 
 * @author Daniel
 */
public class StandardOfficeSectionInputFigure extends AbstractOfficeFloorFigure
		implements OfficeSectionInputFigure {

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link OfficeSectionInputFigureContext}.
	 */
	public StandardOfficeSectionInputFigure(
			OfficeSectionInputFigureContext context) {
		LabelConnectorFigure connector = new LabelConnectorFigure(context
				.getOfficeSectionInputName(), ConnectorDirection.WEST,
				ColorConstants.black);

		// Register the anchors
		ConnectionAnchor anchor = connector.getConnectionAnchor();
		this.registerConnectionAnchor(
				OfficeSectionOutputToOfficeSectionInputModel.class, anchor);

		this.setFigure(connector);
	}

}