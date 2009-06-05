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

import java.util.Map;

import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.officefloor.editparts.DeployedOfficeEditPart;
import net.officefloor.eclipse.wizard.officesource.OfficeInstance;
import net.officefloor.eclipse.wizard.officesource.OfficeSourceWizard;
import net.officefloor.model.change.Change;
import net.officefloor.model.officefloor.DeployedOfficeModel;
import net.officefloor.model.officefloor.OfficeFloorChanges;

/**
 * {@link Operation} to refactor the {@link DeployedOfficeModel}.
 *
 * @author Daniel Sagenschneider
 */
public class RefactorDeployedOfficeFloorChangeOperation extends
		AbstractOfficeFloorChangeOperation<DeployedOfficeEditPart> {

	/**
	 * Initiate.
	 *
	 * @param officeFloorChanges
	 *            {@link OfficeFloorChanges}.
	 */
	public RefactorDeployedOfficeFloorChangeOperation(
			OfficeFloorChanges officeFloorChanges) {
		super("Refactor Office", DeployedOfficeEditPart.class,
				officeFloorChanges);
	}

	/*
	 * ================ AbstractOfficeFloorChangeOperation ===================
	 */

	@Override
	protected Change<?> getChange(OfficeFloorChanges changes, Context context) {

		// Obtain the deployed office
		DeployedOfficeEditPart editPart = context.getEditPart();
		DeployedOfficeModel office = editPart.getCastedModel();

		// Obtain the refactored office
		OfficeInstance officeInstance = OfficeSourceWizard.getOfficeInstance(
				editPart, new OfficeInstance(office));
		if (officeInstance == null) {
			return null; // office not being refactored
		}

		// Obtain the mappings
		Map<String, String> objectNameMappings = officeInstance
				.getObjectNameMapping();
		Map<String, String> inputNameMappings = officeInstance
				.getInputNameMapping();
		Map<String, String> teamNameMappings = officeInstance
				.getTeamNameMapping();

		// Return change to refactor the office
		return changes.refactorDeployedOffice(office, officeInstance
				.getOfficeName(), officeInstance.getOfficeSourceClassName(),
				officeInstance.getOfficeLocation(), officeInstance
						.getPropertylist(), officeInstance.getOfficeType(),
				objectNameMappings, inputNameMappings, teamNameMappings);
	}

}