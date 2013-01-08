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
import net.officefloor.eclipse.woof.editparts.WoofGovernanceEditPart;
import net.officefloor.model.change.Change;
import net.officefloor.model.woof.PropertyModel;
import net.officefloor.model.woof.WoofChanges;
import net.officefloor.model.woof.WoofGovernanceModel;

/**
 * Refactors a {@link WoofGovernanceModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RefactorGovernanceOperation extends
		AbstractWoofChangeOperation<WoofGovernanceEditPart> {

	/**
	 * Initiate.
	 * 
	 * @param woofChanges
	 *            {@link WoofChanges}.
	 */
	public RefactorGovernanceOperation(WoofChanges woofChanges) {
		super("Refactor governance", WoofGovernanceEditPart.class, woofChanges);
	}

	/*
	 * =================== AbstractWoofChangeOperation ====================
	 */

	@Override
	protected Change<?> getChange(WoofChanges changes, Context context) {

		// Obtain the governance to refactor
		WoofGovernanceModel governance = context.getEditPart().getCastedModel();

		// Create the existing governance instance
		GovernanceInstance existing = new GovernanceInstance(
				governance.getWoofGovernanceName(),
				governance.getGovernanceSourceClassName());
		for (PropertyModel property : governance.getProperties()) {
			existing.getPropertyList().addProperty(property.getName())
					.setValue(property.getValue());
		}

		// Obtain the governance instance
		GovernanceInstance instance = GovernanceSourceWizard
				.getGovernanceInstance(context.getEditPart(), existing);
		if (instance == null) {
			return null; // must have governance
		}

		// Obtain governance details
		String governanceName = instance.getGovernanceName();
		String governanceSourceClassName = instance
				.getGovernanceSourceClassName();
		PropertyList properties = instance.getPropertyList();
		GovernanceType<?, ?> governanceType = instance.getGovernanceType();

		// Create change to refactor governance
		Change<WoofGovernanceModel> change = changes.refactorGovernance(
				governance, governanceName, governanceSourceClassName,
				properties, governanceType);

		// Return change to refactor the governance
		return change;
	}

}