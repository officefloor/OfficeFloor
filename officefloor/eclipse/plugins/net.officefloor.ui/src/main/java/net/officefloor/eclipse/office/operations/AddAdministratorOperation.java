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
package net.officefloor.eclipse.office.operations;

import net.officefloor.eclipse.office.editparts.OfficeEditPart;
import net.officefloor.eclipse.wizard.administrationsource.AdministratorInstance;
import net.officefloor.eclipse.wizard.administrationsource.AdministratorSourceWizard;
import net.officefloor.model.change.Change;
import net.officefloor.model.office.AdministratorModel;
import net.officefloor.model.office.OfficeChanges;
import net.officefloor.model.office.OfficeModel;

/**
 * Adds an {@link AdministratorModel} to the {@link OfficeModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class AddAdministratorOperation extends
		AbstractOfficeChangeOperation<OfficeEditPart> {

	/**
	 * Initiate.
	 * 
	 * @param officeChanges
	 *            {@link OfficeChanges}.
	 */
	public AddAdministratorOperation(OfficeChanges officeChanges) {
		super("Add administrator", OfficeEditPart.class, officeChanges);
	}

	/*
	 * ================== AbstractOfficeChangeOperation =====================
	 */

	@Override
	protected Change<?> getChange(OfficeChanges changes, Context context) {

		// Obtain the administrator instance
		AdministratorInstance administrator = AdministratorSourceWizard
				.getAdministratorInstance(context.getEditPart(), null);
		if (administrator == null) {
			return null; // must have administrator
		}

		// Create change to add administrator
		Change<AdministratorModel> change = changes.addAdministrator(
				administrator.getAdministratorName(),
				administrator.getAdministratorSourceClassName(),
				administrator.getPropertylist(),
				administrator.getAdministratorScope(),
				administrator.getAdministratorType());

		// Position the administrator
		context.positionModel(change.getTarget());

		// Return the change to add the administrator
		return change;
	}

}