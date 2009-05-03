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
package net.officefloor.eclipse.section.operations;

import net.officefloor.eclipse.common.action.AbstractOperation;
import net.officefloor.eclipse.common.commands.OfficeFloorCommand;
import net.officefloor.eclipse.section.editparts.SubSectionInputEditPart;
import net.officefloor.model.section.SubSectionInputModel;

/**
 * Toggle {@link SubRoomModel} is public.
 * 
 * @author Daniel
 */
public class ToggleSubSectionInputPublicOperation extends
		AbstractOperation<SubSectionInputEditPart> {

	/**
	 * Initiate.
	 */
	public ToggleSubSectionInputPublicOperation() {
		super("Toggle public", SubSectionInputEditPart.class);
	}

	@Override
	protected void perform(Context context) {

		// Obtain the sub section input flow model
		final SubSectionInputModel subSectionInput = context.getEditPart()
				.getCastedModel();

		// Initial public state
		final boolean initialPublicState = subSectionInput.getIsPublic();

		// Make change
		context.execute(new OfficeFloorCommand() {

			@Override
			protected void doCommand() {
				// Toggle public state
				subSectionInput.setIsPublic(!initialPublicState);
			}

			@Override
			protected void undoCommand() {
				// Reset to initial public state
				subSectionInput.setIsPublic(initialPublicState);
			}
		});
	}

}