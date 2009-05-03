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
package net.officefloor.eclipse.office.operations;

import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.common.operation.AbstractRemoveItemModelOperation;
import net.officefloor.eclipse.office.editparts.OfficeTeamEditPart;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.office.OfficeTeamModel;

/**
 * {@link Operation} to remove the {@link OfficeTeamModel}.
 * 
 * @author Daniel
 */
public class RemoveOfficeTeamOperation
		extends
		AbstractRemoveItemModelOperation<OfficeTeamEditPart, OfficeTeamModel, OfficeModel> {

	/**
	 * Initiate.
	 */
	public RemoveOfficeTeamOperation() {
		super("Remove external team", OfficeTeamEditPart.class);
	}

	@Override
	protected void remove(OfficeTeamModel model, OfficeModel parent) {
		parent.removeOfficeTeam(model);
	}

	@Override
	protected void unremove(OfficeTeamModel model, OfficeModel parent) {
		parent.addOfficeTeam(model);
	}

}