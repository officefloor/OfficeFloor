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

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorConnectionEditPart;
import net.officefloor.eclipse.skin.office.OfficeManagedObjectToOfficeManagedObjectSourceFigureContext;
import net.officefloor.model.office.OfficeManagedObjectToOfficeManagedObjectSourceModel;
import net.officefloor.model.office.OfficeManagedObjectToOfficeManagedObjectSourceModel.OfficeManagedObjectToOfficeManagedObjectSourceEvent;

import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the
 * {@link OfficeManagedObjectToOfficeManagedObjectSourceModel}.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeManagedObjectToOfficeManagedObjectSourceEditPart
		extends
		AbstractOfficeFloorConnectionEditPart<OfficeManagedObjectToOfficeManagedObjectSourceModel, OfficeManagedObjectToOfficeManagedObjectSourceEvent>
		implements OfficeManagedObjectToOfficeManagedObjectSourceFigureContext {

	/*
	 * ============== AbstractOfficeFloorConnectionEditPart ===================
	 */

	@Override
	protected void decorateFigure(PolylineConnection figure) {
		OfficeFloorPlugin.getSkin().getOfficeFigureFactory()
				.decorateOfficeManagedObjectToOfficeManagedObjectSourceFigure(
						figure, this);
	}

}