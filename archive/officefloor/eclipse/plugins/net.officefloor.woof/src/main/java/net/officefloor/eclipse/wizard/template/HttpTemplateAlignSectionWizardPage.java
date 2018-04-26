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

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.conform.ConformInput;
import net.officefloor.model.conform.ConformModel;
import net.officefloor.model.office.OfficeSectionOutputModel;
import net.officefloor.model.woof.WoofTemplateInheritance;
import net.officefloor.woof.model.woof.WoofTemplateModel;
import net.officefloor.woof.model.woof.WoofTemplateOutputModel;

/**
 * {@link IWizardPage} to align refactoring of {@link WoofTemplateModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateAlignSectionWizardPage extends WizardPage {

	/**
	 * {@link WoofTemplateOutputModel} names on existing
	 * {@link WoofTemplateModel}.
	 */
	private final String[] templateOutputs;

	/**
	 * {@link ConformInput} for outputs.
	 */
	private final ConformInput outputConform = new ConformInput();

	/**
	 * Initiate.
	 * 
	 * @param templateInstance
	 *            {@link HttpTemplateInstance}.
	 */
	protected HttpTemplateAlignSectionWizardPage(
			HttpTemplateInstance templateInstance) {
		super("Refactor Template");
		this.setTitle("Refactor Template");

		// Obtain the existing outputs from template
		this.templateOutputs = templateInstance.getTemplateOutputNames();
	}

	/**
	 * Loads the {@link SectionType} for the refactored
	 * {@link WoofTemplateModel}.
	 * 
	 * @param sectionType
	 *            {@link SectionType} for the refactored
	 *            {@link WoofTemplateModel}.
	 * @param superTemplateInheritance
	 *            {@link WoofTemplateInheritance} for the super
	 *            {@link WoofTemplateModel}.
	 */
	public void loadSectionType(SectionType sectionType,
			WoofTemplateInheritance superTemplateInheritance) {

		// Load the output conforms
		List<String> outputs = new LinkedList<String>();
		for (SectionOutputType output : sectionType.getSectionOutputTypes()) {

			// Only include non-escalation (as auto-wire)
			if (output.isEscalationOnly()) {
				continue; // ignore as no escalation output for auto-wire
			}

			// Include the output
			outputs.add(output.getSectionOutputName());
		}

		// Create the listing of inheritable template output names
		String[] inheritableTemplateOutputNames = new String[0];
		if (superTemplateInheritance != null) {
			inheritableTemplateOutputNames = superTemplateInheritance
					.getInheritedWoofTemplateOutputNames().toArray(
							new String[0]);
		}

		// Specify the confirm details
		this.outputConform.setConform(this.templateOutputs,
				outputs.toArray(new String[0]), inheritableTemplateOutputNames);
	}

	/**
	 * Obtains the mapping of {@link OfficeSectionOutput} name to
	 * {@link OfficeSectionOutputModel} name.
	 * 
	 * @return Mapping of {@link OfficeSectionOutput} name to
	 *         {@link OfficeSectionOutputModel} name.
	 */
	public Map<String, String> getOutputNameMapping() {
		return this.outputConform.getTargetItemToExistingItemMapping();
	}

	/**
	 * Obtains the names of the {@link WoofTemplateOutputModel} instances that
	 * are to inherit their configuration.
	 * 
	 * @return names of the {@link WoofTemplateOutputModel} instances that are
	 *         to inherit their configuration.
	 */
	public Set<String> getInheritedWoofTemplateOutputNames() {
		String[] inheritedTemplateOutputNames = this.outputConform
				.getInheritedTargetItemNames();
		return new HashSet<String>(Arrays.asList(inheritedTemplateOutputNames));
	}

	/*
	 * ================= IDialogPage =====================================
	 */

	@Override
	public void createControl(Composite parent) {
		// Create the composite to contain the graphical viewer
		Composite page = new Composite(parent, SWT.NONE);
		page.setLayout(new GridLayout(1, false));

		// Create conform for outputs
		new Label(page, SWT.NONE).setText("Outputs");
		InputHandler<ConformModel> outputHandler = new InputHandler<ConformModel>(
				page, this.outputConform);
		outputHandler.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));

		// Specify the control
		this.setControl(page);
	}

}