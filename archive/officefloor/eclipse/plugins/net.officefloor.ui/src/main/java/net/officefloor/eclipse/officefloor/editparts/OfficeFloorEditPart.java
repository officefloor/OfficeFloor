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
package net.officefloor.eclipse.officefloor.editparts;

import java.util.List;

import org.eclipse.gef.EditPart;

import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorDiagramEditPart;
import net.officefloor.eclipse.common.models.InformationModel;
import net.officefloor.model.officefloor.OfficeFloorModel;

/**
 * {@link EditPart} for the {@link OfficeFloorModel}.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeFloorEditPart extends AbstractOfficeFloorDiagramEditPart<OfficeFloorModel> {

	@Override
	protected void populateChildren(List<Object> childModels) {
		OfficeFloorModel officeFloor = this.getCastedModel();
		childModels.addAll(officeFloor.getDeployedOffices());
		childModels.addAll(officeFloor.getOfficeFloorTeams());
		childModels.addAll(officeFloor.getOfficeFloorManagedObjectSources());
		childModels.addAll(officeFloor.getOfficeFloorInputManagedObjects());
		childModels.addAll(officeFloor.getOfficeFloorManagedObjects());

		// Information that experimental
		childModels.add(new InformationModel("WARNING: The " + this.getEditor().getClass().getSimpleName()
				+ " is only to prove concepts.\n\n" + "It should NEVER be used for application developement.\n\n"
				+ "The purpose of this editor is to prove the raw OfficeFloor model.\n"
				+ "Much of the functionality is not complete for this editor (or likely very buggy).\n\n"
				+ "For application development please use WoOF."));
	}

}