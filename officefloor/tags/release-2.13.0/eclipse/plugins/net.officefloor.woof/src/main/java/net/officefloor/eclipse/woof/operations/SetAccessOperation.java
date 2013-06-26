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

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.wizard.access.AccessInstance;
import net.officefloor.eclipse.wizard.access.HttpSecuritySourceWizard;
import net.officefloor.eclipse.woof.editparts.WoofEditPart;
import net.officefloor.model.change.Change;
import net.officefloor.model.woof.WoofAccessModel;
import net.officefloor.model.woof.WoofChanges;
import net.officefloor.plugin.web.http.security.type.HttpSecurityType;

/**
 * {@link Operation} to specify the {@link WoofAccessModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class SetAccessOperation extends
		AbstractWoofChangeOperation<WoofEditPart> {

	/**
	 * Initiate.
	 * 
	 * @param woofChanges
	 *            {@link WoofChanges}.
	 */
	public SetAccessOperation(WoofChanges woofChanges) {
		super("Set access", WoofEditPart.class, woofChanges);
	}

	/*
	 * ==================== AbstractWoofChangeOperation =================
	 */

	@Override
	protected Change<?> getChange(WoofChanges changes, Context context) {

		// Obtain the access instance
		AccessInstance instance = HttpSecuritySourceWizard.getAccessInstance(
				context.getEditPart(), null);
		if (instance == null) {
			return null; // must have access
		}

		// Obtain access details
		String httpSecuritySourceClassName = instance
				.getHttpSecuritySourceClassName();
		long authenticationTimeout = instance.getAuthenticationTimeout();
		PropertyList properties = instance.getPropertylist();
		HttpSecurityType<?, ?, ?, ?> httpSecurityType = instance
				.getHttpSecurityType();

		// Create the change to specify access
		Change<WoofAccessModel> change = changes.setAccess(
				httpSecuritySourceClassName, authenticationTimeout, properties,
				httpSecurityType);

		// Position access
		context.positionModel(change.getTarget());

		// Return change to specify the access
		return change;
	}

}