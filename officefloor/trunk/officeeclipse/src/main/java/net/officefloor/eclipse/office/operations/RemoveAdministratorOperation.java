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
import net.officefloor.eclipse.office.editparts.AdministratorEditPart;
import net.officefloor.model.office.AdministratorModel;
import net.officefloor.model.office.OfficeModel;

/**
 * {@link Operation} to remove the {@link AdministratorModel}.
 * 
 * @author Daniel
 */
public class RemoveAdministratorOperation
		extends
		AbstractRemoveItemModelOperation<AdministratorEditPart, AdministratorModel, OfficeModel> {

	/**
	 * Initiate.
	 */
	public RemoveAdministratorOperation() {
		super("Remove administrator", AdministratorEditPart.class);
	}

	@Override
	protected void remove(AdministratorModel model, OfficeModel parent) {
		parent.removeOfficeAdministrator(model);
	}

	@Override
	protected void unremove(AdministratorModel model, OfficeModel parent) {
		parent.addOfficeAdministrator(model);
	}

}