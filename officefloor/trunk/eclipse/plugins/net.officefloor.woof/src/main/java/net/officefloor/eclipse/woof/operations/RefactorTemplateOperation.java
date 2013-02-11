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

import net.officefloor.compile.section.SectionType;
import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.wizard.template.HttpTemplateInstance;
import net.officefloor.eclipse.wizard.template.HttpTemplateWizard;
import net.officefloor.eclipse.woof.editparts.WoofTemplateEditPart;
import net.officefloor.model.change.Change;
import net.officefloor.model.woof.WoofChanges;
import net.officefloor.model.woof.WoofTemplateModel;

/**
 * {@link Operation} to refactor a {@link WoofTemplateModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RefactorTemplateOperation extends
		AbstractWoofChangeOperation<WoofTemplateEditPart> {

	/**
	 * Initiate.
	 * 
	 * @param woofChanges
	 *            {@link WoofChanges}.
	 */
	public RefactorTemplateOperation(WoofChanges woofChanges) {
		super("Refactor template", WoofTemplateEditPart.class, woofChanges);
	}

	/*
	 * =================== AbstractWoofChangeOperation ====================
	 */

	@Override
	protected Change<?> getChange(WoofChanges changes, Context context) {

		// Obtain the existing template
		WoofTemplateModel template = context.getEditPart().getCastedModel();

		// Create the instance to aid refactoring
		HttpTemplateInstance existing = new HttpTemplateInstance(template,
				changes);

		// Obtain the template instance
		HttpTemplateInstance instance = HttpTemplateWizard
				.getHttpTemplateInstance(context.getEditPart(), existing);
		if (instance == null) {
			return null; // must have template
		}

		// Obtain the template details
		String uri = instance.getUri();
		String path = instance.getTemplatePath();
		String logicClassName = instance.getLogicClassName();
		SectionType type = instance.getTemplateSectionType();
		boolean isTemplateSecure = instance.isTemplateSecure();
		Map<String, Boolean> linksSecure = instance.getLinksSecure();
		String[] renderRedirectHttpMethods = instance
				.getRenderRedirectHttpMethods();
		String entryPointClassName = instance.getGwtEntryPointClassName();
		String[] serviceAsyncInterfaces = instance
				.getGwtServerAsyncInterfaceNames();
		boolean isEnableComet = instance.isEnableComet();
		String cometManualPublishMethodName = instance
				.getCometManualPublishMethodName();
		Map<String, String> outputNameMapping = instance.getOutputNameMapping();

		// TODO provide is continue rendering
		boolean isContinueRendering = true;

		// Create change to refactor template
		Change<WoofTemplateModel> change = changes.refactorTemplate(template,
				uri, path, logicClassName, type, isTemplateSecure, linksSecure,
				renderRedirectHttpMethods, isContinueRendering,
				entryPointClassName, serviceAsyncInterfaces, isEnableComet,
				cometManualPublishMethodName, outputNameMapping);

		// Return the change to refactor the template
		return change;
	}

}