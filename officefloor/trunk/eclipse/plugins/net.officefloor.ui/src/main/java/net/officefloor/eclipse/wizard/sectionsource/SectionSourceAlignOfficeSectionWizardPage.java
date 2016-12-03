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
package net.officefloor.eclipse.wizard.sectionsource;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import net.officefloor.compile.section.OfficeSectionInputType;
import net.officefloor.compile.section.OfficeSectionObjectType;
import net.officefloor.compile.section.OfficeSectionOutputType;
import net.officefloor.compile.section.OfficeSectionType;
import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.conform.ConformInput;
import net.officefloor.model.conform.ConformModel;
import net.officefloor.model.office.OfficeSectionInputModel;
import net.officefloor.model.office.OfficeSectionModel;
import net.officefloor.model.office.OfficeSectionObjectModel;
import net.officefloor.model.office.OfficeSectionOutputModel;

/**
 * {@link IWizardPage} to align refactoring of {@link OfficeSection}.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionSourceAlignOfficeSectionWizardPage extends WizardPage {

	/**
	 * {@link SectionInstance}.
	 */
	private final SectionInstance sectionInstance;

	/**
	 * Indicates if loading {@link SectionType} or {@link OfficeSection}.
	 */
	private final boolean isLoadType;

	/**
	 * Indicates if refactoring auto-wire instance.
	 */
	private final boolean isAuotWire;

	/**
	 * {@link ConformInput} for inputs.
	 */
	private final ConformInput inputConform = new ConformInput();

	/**
	 * {@link SectionInstance} inputs.
	 */
	private final String[] sectionInputs;

	/**
	 * {@link ConformInput} for outputs.
	 */
	private final ConformInput outputConform = new ConformInput();

	/**
	 * {@link SectionInstance} outputs.
	 */
	private final String[] sectionOutputs;

	/**
	 * {@link ConformInput} for objects.
	 */
	private final ConformInput objectConform = new ConformInput();

	/**
	 * {@link SectionInstance} objects.
	 */
	private final String[] sectionObjects;

	/**
	 * Initiate.
	 * 
	 * @param sectionInstance
	 *            {@link SectionInstance}.
	 * @param isLoadType
	 *            Indicates if loading {@link SectionType} or
	 *            {@link OfficeSection}.
	 * @param isAuotWire
	 *            <code>true</code> if auto-wire section.
	 */
	protected SectionSourceAlignOfficeSectionWizardPage(SectionInstance sectionInstance, boolean isLoadType,
			boolean isAuotWire) {
		super("Refactor Office Section");
		this.sectionInstance = sectionInstance;
		this.isLoadType = isLoadType;
		this.isAuotWire = isAuotWire;
		this.setTitle("Refactor Office Section");

		// Create the inputs/outputs/objects from section
		OfficeSectionModel model = this.sectionInstance.getOfficeSectionModel();
		List<String> inputs = new LinkedList<String>();
		for (OfficeSectionInputModel input : model.getOfficeSectionInputs()) {
			inputs.add(input.getOfficeSectionInputName());
		}
		this.sectionInputs = inputs.toArray(new String[0]);
		List<String> outputs = new LinkedList<String>();
		for (OfficeSectionOutputModel output : model.getOfficeSectionOutputs()) {
			outputs.add(output.getOfficeSectionOutputName());
		}
		this.sectionOutputs = outputs.toArray(new String[0]);
		List<String> objects = new LinkedList<String>();
		for (OfficeSectionObjectModel object : model.getOfficeSectionObjects()) {
			objects.add(object.getOfficeSectionObjectName());
		}
		this.sectionObjects = objects.toArray(new String[0]);
	}

	/**
	 * Loads the {@link SectionSourceInstance}.
	 * 
	 * @param instance
	 *            {@link SectionSourceInstance}.
	 */
	public void loadSectionSourceInstance(SectionSourceInstance instance) {

		// Determine if SectionType or OfficeSection
		if (this.isLoadType) {
			// Ensure obtain section type (may still require properties)
			SectionType sectionType = instance.getSectionType();
			if (sectionType == null) {
				return;
			}

			// Load the input conforms
			List<String> inputs = new LinkedList<String>();
			for (SectionInputType input : sectionType.getSectionInputTypes()) {
				inputs.add(input.getSectionInputName());
			}
			this.inputConform.setConform(this.sectionInputs, inputs.toArray(new String[0]));

			// Load the output conforms
			List<String> outputs = new LinkedList<String>();
			for (SectionOutputType output : sectionType.getSectionOutputTypes()) {

				// Only include is not auto-wire and non-escalation
				if ((this.isAuotWire) && (output.isEscalationOnly())) {
					continue; // ignore as no escalation output for auto-wire
				}

				// Include the output
				outputs.add(output.getSectionOutputName());
			}
			this.outputConform.setConform(this.sectionOutputs, outputs.toArray(new String[0]));

			// Only load objects if not auto-wire
			if (!this.isAuotWire) {
				// Load the object conforms
				List<String> objects = new LinkedList<String>();
				for (SectionObjectType object : sectionType.getSectionObjectTypes()) {
					objects.add(object.getSectionObjectName());
				}
				this.objectConform.setConform(this.sectionObjects, objects.toArray(new String[0]));
			}

		} else {
			// Ensure obtain office section (may still require properties)
			OfficeSectionType officeSectionType = instance.getOfficeSectionType();
			if (officeSectionType == null) {
				return;
			}

			// Load the input conforms
			List<String> inputs = new LinkedList<String>();
			for (OfficeSectionInputType inputType : officeSectionType.getOfficeSectionInputTypes()) {
				inputs.add(inputType.getOfficeSectionInputName());
			}
			this.inputConform.setConform(this.sectionInputs, inputs.toArray(new String[0]));

			// Load the output conforms
			List<String> outputs = new LinkedList<String>();
			for (OfficeSectionOutputType outputType : officeSectionType.getOfficeSectionOutputTypes()) {
				outputs.add(outputType.getOfficeSectionOutputName());
			}
			this.outputConform.setConform(this.sectionOutputs, outputs.toArray(new String[0]));

			// Load the object conforms
			List<String> objects = new LinkedList<String>();
			for (OfficeSectionObjectType objectType : officeSectionType.getOfficeSectionObjectTypes()) {
				objects.add(objectType.getOfficeSectionObjectName());
			}
			this.objectConform.setConform(this.sectionObjects, objects.toArray(new String[0]));
		}
	}

	/**
	 * Obtains the mapping of {@link OfficeSectionInput} name to
	 * {@link OfficeSectionInputModel} name.
	 * 
	 * @return Mapping of {@link OfficeSectionInput} name to
	 *         {@link OfficeSectionInputModel} name.
	 */
	public Map<String, String> getInputNameMapping() {
		return this.inputConform.getTargetItemToExistingItemMapping();
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
	 * Obtains the mapping of {@link OfficeSectionObject} name to
	 * {@link OfficeSectionObjectModel} name.
	 * 
	 * @return Mapping of {@link OfficeSectionObject} name to
	 *         {@link OfficeSectionObjectModel} name.
	 */
	public Map<String, String> getObjectNameMapping() {
		return this.objectConform.getTargetItemToExistingItemMapping();
	}

	/*
	 * ================= IDialogPage =====================================
	 */

	@Override
	public void createControl(Composite parent) {
		// Create the composite to contain the graphical viewer
		Composite page = new Composite(parent, SWT.NONE);
		page.setLayout(new GridLayout(1, false));

		// Create conform for inputs
		new Label(page, SWT.NONE).setText("Inputs");
		InputHandler<ConformModel> inputHandler = new InputHandler<ConformModel>(page, this.inputConform);
		inputHandler.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Create conform for outputs
		new Label(page, SWT.NONE).setText("Outputs");
		InputHandler<ConformModel> outputHandler = new InputHandler<ConformModel>(page, this.outputConform);
		outputHandler.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Create conform for objects (if not auto-wire)
		if (!this.isAuotWire) {
			new Label(page, SWT.NONE).setText("Objects");
			InputHandler<ConformModel> objectHandler = new InputHandler<ConformModel>(page, this.objectConform);
			objectHandler.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		}

		// Specify the control
		this.setControl(page);
	}

}