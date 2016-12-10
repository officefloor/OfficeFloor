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
package net.officefloor.eclipse.skin.standard.officefloor;

import net.officefloor.eclipse.skin.officefloor.DeployedOfficeInputFigure;
import net.officefloor.eclipse.skin.officefloor.DeployedOfficeInputFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.eclipse.skin.standard.figure.OfficeItemFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.officefloor.OfficeFloorChanges;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel;

import org.eclipse.draw2d.ConnectionAnchor;

/**
 * Standard {@link DeployedOfficeInputFigure}.
 *
 * @author Daniel Sagenschneider
 */
public class StandardDeployedOfficeInputFigure extends
		AbstractOfficeFloorFigure implements DeployedOfficeInputFigure {

	/**
	 * {@link OfficeItemFigure}.
	 */
	private final OfficeItemFigure figure;

	/**
	 * Initiate.
	 *
	 * @param context
	 *            {@link DeployedOfficeInputFigureContext}.
	 */
	public StandardDeployedOfficeInputFigure(
			DeployedOfficeInputFigureContext context) {

		// Obtain the name of the input
		String inputName = context.getOfficeSectionName()
				+ OfficeFloorChanges.SECTION_INPUT_SEPARATOR
				+ context.getOfficeSectionInputName();

		this.figure = new OfficeItemFigure(inputName, ConnectorDirection.EAST,
				StandardOfficeFloorColours.BLACK());

		// Register connections
		ConnectionAnchor anchor = this.figure.getConnectionAnchor();
		this
				.registerConnectionAnchor(
						OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel.class,
						anchor);

		this.setFigure(this.figure);
	}

	/*
	 * =================== DeployedOfficeInputFigure ===========================
	 */

	@Override
	public void setSectionInput(String sectionName, String sectionInputName) {
		String inputName = sectionName
				+ OfficeFloorChanges.SECTION_INPUT_SEPARATOR + sectionInputName;
		this.figure.setItemName(inputName);
	}

}