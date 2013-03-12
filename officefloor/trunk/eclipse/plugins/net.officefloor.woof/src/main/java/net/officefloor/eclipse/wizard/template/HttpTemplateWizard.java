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
package net.officefloor.eclipse.wizard.template;

import java.util.Map;
import java.util.Set;

import net.officefloor.compile.section.SectionType;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.repository.project.ProjectConfigurationContext;
import net.officefloor.eclipse.wizard.WizardUtil;
import net.officefloor.model.woof.WoofTemplateInheritance;
import net.officefloor.model.woof.WoofTemplateModel;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;
import net.officefloor.plugin.woof.WoofOfficeFloorSource;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.part.EditorPart;

/**
 * Wizard to add and manage {@link HttpTemplate}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateWizard extends Wizard {

	/**
	 * Facade method to obtain the {@link HttpTemplateInstance}.
	 * 
	 * @param editPart
	 *            {@link AbstractOfficeFloorEditPart} to obtain necessary
	 *            objects to run the {@link HttpTemplateWizard}.
	 * @param templateInstance
	 *            {@link HttpTemplateInstance} to based decisions. May be
	 *            <code>null</code> if creating a new
	 *            {@link HttpTemplateInstance}.
	 * @param templateInheritances
	 *            {@link WoofTemplateInheritance} instances by their
	 *            {@link WoofTemplateModel} name.
	 * @return {@link HttpTemplateInstance} or <code>null</code> if cancelled.
	 */
	public static HttpTemplateInstance getHttpTemplateInstance(
			AbstractOfficeFloorEditPart<?, ?, ?> editPart,
			HttpTemplateInstance templateInstance,
			Map<String, WoofTemplateInheritance> templateInheritances) {

		// Create and run the wizard
		HttpTemplateWizard wizard = new HttpTemplateWizard(editPart,
				templateInstance, templateInheritances);
		if (WizardUtil.runWizard(wizard, editPart)) {
			// Successful so return the HTTP Template instance
			return wizard.getHttpTemplateInstance();
		} else {
			// Cancelled so no instance
			return null;
		}
	}

	/**
	 * {@link HttpTemplateWizardPage}.
	 */
	private final HttpTemplateWizardPage templatePage;

	/**
	 * {@link HttpTemplateAlignSectionWizardPage}.
	 */
	private final HttpTemplateAlignSectionWizardPage templateAlignPage;

	/**
	 * {@link HttpTemplateInstance}.
	 */
	private HttpTemplateInstance httpTemplateInstance = null;

	/**
	 * Initiate.
	 * 
	 * @param editPart
	 *            {@link AbstractOfficeFloorEditPart}.
	 * @param templateInstance
	 *            {@link HttpTemplateInstance} to base decisions on. May be
	 *            <code>null</code> for creating a new
	 *            {@link HttpTemplateInstance}.
	 * @param templateInheritances
	 *            {@link WoofTemplateInheritance} instances by their
	 *            {@link WoofTemplateModel} name.
	 */
	public HttpTemplateWizard(AbstractOfficeFloorEditPart<?, ?, ?> editPart,
			HttpTemplateInstance templateInstance,
			Map<String, WoofTemplateInheritance> templateInheritances) {

		// Obtain the project
		EditorPart editorPart = editPart.getEditor();
		IProject project = ProjectConfigurationContext.getProject(editorPart
				.getEditorInput());

		// Create the HTTP template wizard page
		this.templatePage = new HttpTemplateWizardPage(project, editPart,
				templateInstance, templateInheritances);

		// Determine if refactoring template
		if (templateInstance != null) {
			// Create the align page
			this.templateAlignPage = new HttpTemplateAlignSectionWizardPage(
					templateInstance);

		} else {
			// Creating new template
			this.templateAlignPage = null;
		}
	}

	/**
	 * Obtains the {@link HttpTemplateInstance}.
	 * 
	 * @return {@link HttpTemplateInstance}.
	 */
	public HttpTemplateInstance getHttpTemplateInstance() {
		return this.httpTemplateInstance;
	}

	/*
	 * ======================= Wizard =========================
	 */

	@Override
	public void addPages() {
		// Add template details page
		this.addPage(this.templatePage);

		// Add refactoring outputs page
		if (this.templateAlignPage != null) {
			this.addPage(this.templateAlignPage);
		}
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {

		// Determine if currently template page
		if (page == this.templatePage) {

			// Determine if refactoring
			if (this.templateAlignPage != null) {
				// Refactor template outputs
				this.templateAlignPage.loadSectionType(
						this.templatePage.getSectionType(),
						this.templatePage.getSuperTemplateInheritance());
				return this.templateAlignPage;
			}
		}

		// As here no further pages
		return null;
	}

	@Override
	public boolean canFinish() {

		// Ensure template page is complete
		if (!(this.templatePage.isPageComplete())) {
			return false;
		}

		// Ensure refactoring is complete
		if ((this.templateAlignPage != null)
				&& (!this.templatePage.isPageComplete())) {
			return false;
		}

		// As here, able to finish
		return true;
	}

	@Override
	public boolean performFinish() {

		// Obtain the HTTP Template details
		String uriPath = this.templatePage.getUriPath();
		String templatePath = this.templatePage.getTemplatePath();
		String logicClassName = this.templatePage.getLogicClassName();
		SectionType sectionType = this.templatePage.getSectionType();
		boolean isTemplateSecure = this.templatePage.isTemplateSecure();
		Map<String, Boolean> linksSecure = this.templatePage.getLinksSecure();
		String[] renderRedirectHttpMethods = this.templatePage
				.getRenderRedirectHttpMethods();
		boolean isContinueRendering = this.templatePage.isContinueRendering();
		String gwtEntryPointClassName = this.templatePage
				.getGwtEntryPointClassName();
		String[] gwtServerAsyncInterfaceNames = this.templatePage
				.getGwtAsyncInterfaceNames();
		boolean isEnableComet = this.templatePage.isEnableComet();
		String cometManualPublishMethodName = this.templatePage
				.getCometManualPublishMethodName();

		// Calculate the template name
		String woofTemplateName = WoofOfficeFloorSource
				.getTemplateSectionName(uriPath);

		// Obtain the inheritance details
		WoofTemplateInheritance superTempateInheritance = this.templatePage
				.getSuperTemplateInheritance();
		WoofTemplateModel superTemplate = null;
		Set<String> inheritedTemplateOutputNames = null;
		if (superTempateInheritance != null) {
			// Provide the super template
			superTemplate = superTempateInheritance.getSuperTemplate();

			// Default the inherited output configuration (add template)
			inheritedTemplateOutputNames = superTempateInheritance
					.getInheritedWoofTemplateOutputNames();
		}

		// Obtain the refactor details
		Map<String, String> outputNameMapping = null;
		if (this.templateAlignPage != null) {
			// Specify the output name mapping
			outputNameMapping = this.templateAlignPage.getOutputNameMapping();

			// Specify inherited output configuration (refactor template)
			inheritedTemplateOutputNames = this.templateAlignPage
					.getInheritedWoofTemplateOutputNames();
		}

		// Create HTTP Template Instance
		this.httpTemplateInstance = new HttpTemplateInstance(woofTemplateName,
				uriPath, templatePath, logicClassName, sectionType,
				superTemplate, inheritedTemplateOutputNames, isTemplateSecure,
				linksSecure, renderRedirectHttpMethods, isContinueRendering,
				gwtEntryPointClassName, gwtServerAsyncInterfaceNames,
				isEnableComet, cometManualPublishMethodName, outputNameMapping);

		// Use the HTTP Template Instance
		return true;
	}

}