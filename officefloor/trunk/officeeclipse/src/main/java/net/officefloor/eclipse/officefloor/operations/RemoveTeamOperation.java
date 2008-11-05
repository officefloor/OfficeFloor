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
import net.officefloor.model.officefloor.TeamModel;

/**
 * {@link Operation} to remove the {@link TeamModel}.
 * 
 * @author Daniel
 */
public class RemoveTeamOperation
		extends
		AbstractRemoveItemModelOperation<TeamEditPart, TeamModel, OfficeFloorModel> {

	/**
	 * Initiate.
	 */
	public RemoveTeamOperation() {
		super("Remove team", TeamEditPart.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.operation.AbstractRemoveItemModelOperation
	 * #remove(net.officefloor.model.ItemModel, net.officefloor.model.Model)
	 */
	@Override
	protected void remove(TeamModel model, OfficeFloorModel parent) {
		parent.removeTeam(model);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.operation.AbstractRemoveItemModelOperation
	 * #unremove(net.officefloor.model.ItemModel, net.officefloor.model.Model)
	 */
	@Override
	protected void unremove(TeamModel model, OfficeFloorModel parent) {
		parent.addTeam(model);
	}

}
