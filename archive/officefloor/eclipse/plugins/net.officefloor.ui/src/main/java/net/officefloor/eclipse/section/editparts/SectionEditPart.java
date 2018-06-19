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
package net.officefloor.eclipse.section.editparts;

import java.util.List;

import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorDiagramEditPart;
import net.officefloor.eclipse.common.models.InformationModel;
import net.officefloor.model.section.SectionModel;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link SectionModel}.
 *
 * @author Daniel Sagenschneider
 */
public class SectionEditPart extends AbstractOfficeFloorDiagramEditPart<SectionModel> {

	@Override
	protected void populateChildren(List<Object> childModels) {
		SectionModel section = this.getCastedModel();
		childModels.addAll(section.getSubSections());
		childModels.addAll(section.getExternalManagedObjects());
		childModels.addAll(section.getSectionManagedObjectSources());
		childModels.addAll(section.getSectionManagedObjects());
		childModels.addAll(section.getExternalFlows());

		// Information that experimental
		childModels.add(new InformationModel("WARNING: The " + this.getEditor().getClass().getSimpleName()
				+ " is only to prove concepts.\n\n" + "It should NEVER be used for application developement.\n\n"
				+ "The purpose of this editor is to prove the raw OfficeFloor model.\n"
				+ "Much of the functionality is not complete for this editor (or likely very buggy).\n\n"
				+ "For application development please use WoOF."));
	}

}