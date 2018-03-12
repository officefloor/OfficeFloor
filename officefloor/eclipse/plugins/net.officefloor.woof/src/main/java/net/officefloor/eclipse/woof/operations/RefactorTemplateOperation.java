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
import java.util.Set;

import net.officefloor.compile.section.SectionType;
import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.wizard.template.HttpTemplateInstance;
import net.officefloor.eclipse.wizard.template.HttpTemplateWizard;
import net.officefloor.eclipse.woof.WoofEditor;
import net.officefloor.eclipse.woof.editparts.WoofTemplateEditPart;
import net.officefloor.model.change.Change;
import net.officefloor.model.woof.WoofTemplateInheritance;
import net.officefloor.woof.model.woof.WoofChanges;
import net.officefloor.woof.model.woof.WoofTemplateChangeContext;
import net.officefloor.woof.model.woof.WoofTemplateExtension;
import net.officefloor.woof.model.woof.WoofTemplateModel;

/**
 * {@link Operation} to refactor a {@link WoofTemplateModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RefactorTemplateOperation extends AbstractWoofChangeOperation<WoofTemplateEditPart> {

	/**
	 * {@link WoofEditor}.
	 */
	private final WoofEditor editor;

	/**
	 * Initiate.
	 * 
	 * @param woofChanges
	 *            {@link WoofChanges}.
	 * @param editor
	 *            {@link WoofEditor}.
	 */
	public RefactorTemplateOperation(WoofChanges woofChanges, WoofEditor editor) {
		super("Refactor template", WoofTemplateEditPart.class, woofChanges);
		this.editor = editor;
	}

	/*
	 * =================== AbstractWoofChangeOperation ====================
	 */

	@Override
	protected Change<?> getChange(WoofChanges changes, Context context) {

		// Obtain the existing template
		WoofTemplateModel template = context.getEditPart().getCastedModel();

		// Create the instance to aid refactoring
		HttpTemplateInstance existing = new HttpTemplateInstance(template, changes);

		// Obtain the template inheritances
		Map<String, WoofTemplateInheritance> templateInheritances = changes.getWoofTemplateInheritances();

		// Obtain the template instance
		HttpTemplateInstance instance = HttpTemplateWizard.getHttpTemplateInstance(context.getEditPart(), existing,
				templateInheritances);
		if (instance == null) {
			return null; // must have template
		}

		// Obtain the template details
		String uri = instance.getUri();
		String path = instance.getTemplatePath();
		String logicClassName = instance.getLogicClassName();
		SectionType type = instance.getTemplateSectionType();
		WoofTemplateModel superTemplate = instance.getSuperTemplate();
		Set<String> inheritedTemplateOutputNames = instance.getInheritedTemplateOutputNames();
		String contentType = instance.getContentType();
		boolean isTemplateSecure = instance.isTemplateSecure();
		Map<String, Boolean> linksSecure = instance.getLinksSecure();
		String[] renderRedirectHttpMethods = instance.getRenderRedirectHttpMethods();
		boolean isContinueRendering = instance.isContinueRendering();
		Map<String, String> outputNameMapping = instance.getOutputNameMapping();

		// Obtain the extensions
		WoofTemplateExtension[] extensions = instance.getWoofTemplateExtensions();

		// Obtain the change context
		WoofTemplateChangeContext changeContext = this.editor.getWoofTemplateChangeContext();

		// Create change to refactor template
		Change<WoofTemplateModel> change = changes.refactorTemplate(template, uri, path, logicClassName, type,
				superTemplate, inheritedTemplateOutputNames, contentType, isTemplateSecure, linksSecure,
				renderRedirectHttpMethods, isContinueRendering, extensions, outputNameMapping, changeContext);

		// Return the change to refactor the template
		return change;
	}

}