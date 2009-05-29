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
package net.officefloor.eclipse.section.operations;

import net.officefloor.eclipse.section.editparts.SubSectionInputEditPart;
import net.officefloor.model.change.Change;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SubSectionInputModel;

/**
 * Toggle {@link SubSectionInputModel} public.
 * 
 * @author Daniel Sagenschneider
 */
public class ToggleSubSectionInputPublicOperation extends
		AbstractSectionChangeOperation<SubSectionInputEditPart> {

	/**
	 * Initiate.
	 * 
	 * @param sectionChanges
	 *            {@link SectionChanges}.
	 */
	public ToggleSubSectionInputPublicOperation(SectionChanges sectionChanges) {
		super("Toggle public", SubSectionInputEditPart.class, sectionChanges);
	}

	/*
	 * ================= AbstractSectionChangeOperation =====================
	 */

	@Override
	protected Change<?> getChange(SectionChanges changes, Context context) {

		// Obtain the sub section input
		SubSectionInputModel input = context.getEditPart().getCastedModel();

		// Toggle input public
		return changes.setSubSectionInputPublic(!input.getIsPublic(), null,
				input);
	}

}