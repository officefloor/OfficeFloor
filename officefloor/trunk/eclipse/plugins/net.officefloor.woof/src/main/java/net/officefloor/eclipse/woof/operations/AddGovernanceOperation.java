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
package net.officefloor.eclipse.woof.operations;

import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.wizard.governancesource.GovernanceInstance;
import net.officefloor.eclipse.wizard.governancesource.GovernanceSourceWizard;
import net.officefloor.eclipse.woof.editparts.WoofEditPart;
import net.officefloor.model.change.Change;
import net.officefloor.model.woof.WoofChanges;
import net.officefloor.model.woof.WoofGovernanceModel;
import net.officefloor.model.woof.WoofModel;

/**
 * Adds a {@link WoofGovernanceModel} to the {@link WoofModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class AddGovernanceOperation extends
		AbstractWoofChangeOperation<WoofEditPart> {

	/**
	 * Initiate.
	 * 
	 * @param woofChanges
	 *            {@link WoofChanges}.
	 */
	public AddGovernanceOperation(WoofChanges woofChanges) {
		super("Add governance", WoofEditPart.class, woofChanges);
	}

	/*
	 * =================== AbstractWoofChangeOperation ====================
	 */

	@Override
	protected Change<?> getChange(WoofChanges changes, Context context) {

		// Obtain the governance instance
		GovernanceInstance instance = GovernanceSourceWizard
				.getGovernanceInstance(context.getEditPart(), null);
		if (instance == null) {
			return null; // must have governance
		}

		// Obtain governance details
		String governanceName = instance.getGovernanceName();
		String governanceSourceClassName = instance
				.getGovernanceSourceClassName();
		PropertyList properties = instance.getPropertyList();
		GovernanceType<?, ?> governanceType = instance.getGovernanceType();

		// Create change to add governance
		Change<WoofGovernanceModel> change = changes.addGovernance(
				governanceName, governanceSourceClassName, properties,
				governanceType);

		// Position governance
		context.positionModel(change.getTarget());

		// Return change to add the governance
		return change;
	}

}