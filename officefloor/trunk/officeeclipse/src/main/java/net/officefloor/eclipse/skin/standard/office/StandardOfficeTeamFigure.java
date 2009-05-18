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

import net.officefloor.eclipse.skin.office.OfficeTeamFigure;
import net.officefloor.eclipse.skin.office.OfficeTeamFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.figure.LabelConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.office.AdministratorToOfficeTeamModel;
import net.officefloor.model.office.OfficeSectionResponsibilityToOfficeTeamModel;
import net.officefloor.model.office.OfficeTeamModel;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;

/**
 * Standard {@link OfficeTeamFigure}.
 * 
 * @author Daniel
 */
public class StandardOfficeTeamFigure extends AbstractOfficeFloorFigure
		implements OfficeTeamFigure {

	/**
	 * {@link Label} containing the {@link OfficeTeamModel} name.
	 */
	private final Label officeTeamName;

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link OfficeTeamFigureContext}.
	 */
	public StandardOfficeTeamFigure(OfficeTeamFigureContext context) {
		LabelConnectorFigure figure = new LabelConnectorFigure(context
				.getOfficeTeamName(), ConnectorDirection.WEST,
				ColorConstants.black);
		this.officeTeamName = figure.getLabel();

		// Register the anchors
		ConnectionAnchor anchor = figure.getConnectionAnchor();
		this.registerConnectionAnchor(
				OfficeSectionResponsibilityToOfficeTeamModel.class, anchor);
		this.registerConnectionAnchor(AdministratorToOfficeTeamModel.class,
				anchor);

		this.setFigure(figure);
	}

	/*
	 * ================== OfficeTeamFigure ===================================
	 */

	@Override
	public void setOfficeTeamName(String officeTeamName) {
		this.officeTeamName.setText(officeTeamName);
	}

	@Override
	public IFigure getOfficeTeamNameFigure() {
		return this.officeTeamName;
	}

}