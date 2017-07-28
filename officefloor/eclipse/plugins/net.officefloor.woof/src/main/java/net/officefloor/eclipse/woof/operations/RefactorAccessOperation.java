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

import java.util.Map;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.wizard.access.AccessInstance;
import net.officefloor.eclipse.wizard.access.HttpSecuritySourceWizard;
import net.officefloor.eclipse.woof.editparts.WoofAccessEditPart;
import net.officefloor.model.change.Change;
import net.officefloor.model.woof.WoofAccessModel;
import net.officefloor.model.woof.WoofChanges;
import net.officefloor.plugin.web.http.security.type.HttpSecurityType;

/**
 * {@link Operation} to refactor a {@link WoofAccessModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RefactorAccessOperation extends AbstractWoofChangeOperation<WoofAccessEditPart> {

	/**
	 * Initiate.
	 * 
	 * @param woofChanges
	 *            {@link WoofChanges}.
	 */
	public RefactorAccessOperation(WoofChanges woofChanges) {
		super("Refactor access", WoofAccessEditPart.class, woofChanges);
	}

	/*
	 * ==================== AbstractWoofChangeOperation =================
	 */

	@Override
	protected Change<?> getChange(WoofChanges changes, Context context) {

		// Obtain the access to refactor
		WoofAccessModel access = context.getEditPart().getCastedModel();

		// Obtain the refactored access instance
		AccessInstance instance = HttpSecuritySourceWizard.getAccessInstance(context.getEditPart(),
				new AccessInstance(access));
		if (instance == null) {
			return null; // must have access
		}

		// Obtain section details
		String accessName = instance.getAccessName();
		String httpSecuritySourceClassName = instance.getHttpSecuritySourceClassName();
		long authenticationTimeout = instance.getAuthenticationTimeout();
		PropertyList properties = instance.getPropertylist();
		HttpSecurityType<?, ?, ?, ?> httpSecurityType = instance.getHttpSecurityType();
		Map<String, String> outputNameMapping = instance.getOutputNameMapping();

		// Create change to refactor access
		Change<WoofAccessModel> change = changes.refactorAccess(access, accessName, httpSecuritySourceClassName,
				authenticationTimeout, properties, httpSecurityType, outputNameMapping);

		// Position section
		context.positionModel(change.getTarget());

		// Return change to add the section
		return change;
	}

}