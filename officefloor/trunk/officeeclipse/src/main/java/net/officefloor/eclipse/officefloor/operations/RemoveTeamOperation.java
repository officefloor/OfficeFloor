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
package net.officefloor.eclipse.officefloor.operations;

import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.common.operation.AbstractRemoveItemModelOperation;
import net.officefloor.eclipse.officefloor.editparts.TeamEditPart;
import net.officefloor.model.officefloor.OfficeFloorModel;
import net.officefloor.model.officefloor.OfficeFloorTeamModel;

/**
 * {@link Operation} to remove the {@link OfficeFloorTeamModel}.
 * 
 * @author Daniel
 */
public class RemoveTeamOperation
		extends
		AbstractRemoveItemModelOperation<TeamEditPart, OfficeFloorTeamModel, OfficeFloorModel> {

	/**
	 * Initiate.
	 */
	public RemoveTeamOperation() {
		super("Remove team", TeamEditPart.class);
	}

	@Override
	protected void remove(OfficeFloorTeamModel model, OfficeFloorModel parent) {
		parent.removeOfficeFloorTeam(model);
	}

	@Override
	protected void unremove(OfficeFloorTeamModel model, OfficeFloorModel parent) {
		parent.addOfficeFloorTeam(model);
	}

}