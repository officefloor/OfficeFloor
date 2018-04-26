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
import net.officefloor.eclipse.wizard.security.HttpSecuritySourceWizard;
import net.officefloor.eclipse.wizard.security.SecurityInstance;
import net.officefloor.eclipse.woof.editparts.WoofSecurityEditPart;
import net.officefloor.model.change.Change;
import net.officefloor.web.security.type.HttpSecurityType;
import net.officefloor.woof.model.woof.WoofChanges;
import net.officefloor.woof.model.woof.WoofSecurityModel;

/**
 * {@link Operation} to refactor a {@link WoofSecurityModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RefactorSecurityOperation extends AbstractWoofChangeOperation<WoofSecurityEditPart> {

	/**
	 * Initiate.
	 * 
	 * @param woofChanges
	 *            {@link WoofChanges}.
	 */
	public RefactorSecurityOperation(WoofChanges woofChanges) {
		super("Refactor security", WoofSecurityEditPart.class, woofChanges);
	}

	/*
	 * ==================== AbstractWoofChangeOperation =================
	 */

	@Override
	protected Change<?> getChange(WoofChanges changes, Context context) {

		// Obtain the security to refactor
		WoofSecurityModel security = context.getEditPart().getCastedModel();

		// Obtain the refactored security instance
		SecurityInstance instance = HttpSecuritySourceWizard.getSecurityInstance(context.getEditPart(),
				new SecurityInstance(security));
		if (instance == null) {
			return null; // must have access
		}

		// Obtain security details
		String securityName = instance.getSecurityName();
		String httpSecuritySourceClassName = instance.getHttpSecuritySourceClassName();
		long authenticationTimeout = instance.getAuthenticationTimeout();
		String[] contentTypes = instance.getContentTypes();
		PropertyList properties = instance.getPropertylist();
		HttpSecurityType<?, ?, ?, ?, ?> httpSecurityType = instance.getHttpSecurityType();
		Map<String, String> outputNameMapping = instance.getOutputNameMapping();

		// Create change to refactor security
		Change<WoofSecurityModel> change = changes.refactorSecurity(security, securityName, httpSecuritySourceClassName,
				authenticationTimeout, properties, contentTypes, httpSecurityType, outputNameMapping);

		// Position section
		context.positionModel(change.getTarget());

		// Return change to add the section
		return change;
	}

}