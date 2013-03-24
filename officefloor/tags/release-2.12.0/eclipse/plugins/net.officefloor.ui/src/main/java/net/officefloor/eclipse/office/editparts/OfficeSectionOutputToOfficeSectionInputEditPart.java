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
package net.officefloor.eclipse.office.editparts;

import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gef.EditPart;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorConnectionEditPart;
import net.officefloor.eclipse.skin.office.OfficeSectionOutputToOfficeSectionInputFigureContext;
import net.officefloor.model.office.OfficeSectionOutputToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeSectionOutputToOfficeSectionInputModel.OfficeSectionOutputToOfficeSectionInputEvent;

/**
 * {@link EditPart} for {@link OfficeSectionOutputToOfficeSectionInputModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeSectionOutputToOfficeSectionInputEditPart
		extends
		AbstractOfficeFloorConnectionEditPart<OfficeSectionOutputToOfficeSectionInputModel, OfficeSectionOutputToOfficeSectionInputEvent>
		implements OfficeSectionOutputToOfficeSectionInputFigureContext {

	/*
	 * =============== AbstractOfficeFloorConnectionEditPart ===================
	 */

	@Override
	protected void decorateFigure(PolylineConnection figure) {
		OfficeFloorPlugin.getSkin().getOfficeFigureFactory()
				.decorateOfficeSectionOutputToOfficeSectionInput(figure, this);
	}

}