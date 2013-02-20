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
package net.officefloor.eclipse.wizard.access;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.conform.ConformInput;
import net.officefloor.model.conform.ConformModel;
import net.officefloor.model.woof.WoofAccessInputModel;
import net.officefloor.model.woof.WoofAccessModel;
import net.officefloor.model.woof.WoofAccessOutputModel;
import net.officefloor.plugin.web.http.security.HttpSecuritySectionSource;
import net.officefloor.plugin.web.http.security.type.HttpSecurityFlowType;
import net.officefloor.plugin.web.http.security.type.HttpSecurityType;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * {@link IWizardPage} to align refactoring of {@link AccessInstance}.
 * 
 * @author Daniel Sagenschneider
 */
public class AccessAlignWizardPage extends WizardPage {

	/**
	 * {@link AccessInstance}.
	 */
	private final AccessInstance accessInstance;

	/**
	 * {@link ConformInput} for inputs.
	 */
	private final ConformInput inputConform = new ConformInput();

	/**
	 * {@link AccessInstance} inputs.
	 */
	private final String[] accessInputs;

	/**
	 * {@link ConformInput} for outputs.
	 */
	private final ConformInput outputConform = new ConformInput();

	/**
	 * {@link AccessInstance} outputs.
	 */
	private final String[] accessOutputs;

	/**
	 * Initiate.
	 * 
	 * @param accessInstance
	 *            {@link AccessInstance}.
	 */
	protected AccessAlignWizardPage(AccessInstance accessInstance) {
		super("Refactor Access");
		this.accessInstance = accessInstance;
		this.setTitle("Refactor Access");

		// Create the inputs/outputs from section
		WoofAccessModel model = this.accessInstance.getWoofAccessModel();
		List<String> inputs = new LinkedList<String>();
		for (WoofAccessInputModel input : model.getInputs()) {
			inputs.add(input.getWoofAccessInputName());
		}
		this.accessInputs = inputs.toArray(new String[0]);
		List<String> outputs = new LinkedList<String>();
		for (WoofAccessOutputModel output : model.getOutputs()) {
			outputs.add(output.getWoofAccessOutputName());
		}
		this.accessOutputs = outputs.toArray(new String[0]);
	}

	/**
	 * Loads the {@link HttpSecuritySourceInstance}.
	 * 
	 * @param instance
	 *            {@link HttpSecuritySourceInstance}.
	 */
	public void loadHttpSecuritySourceInstance(
			HttpSecuritySourceInstance instance) {

		// Ensure obtain HTTP Security type (may still require properties)
		HttpSecurityType<?, ?, ?, ?> httpSecurityType = instance
				.getHttpSecurityType();
		if (httpSecurityType == null) {
			return;
		}

		// Obtain the output flows
		HttpSecurityFlowType<?>[] flowTypes = httpSecurityType.getFlowTypes();

		// Load the input conforms
		List<String> inputs = new LinkedList<String>();
		if (flowTypes.length > 0) {
			inputs.add(HttpSecuritySectionSource.INPUT_AUTHENTICATE);
		}
		this.inputConform.setConform(this.accessInputs,
				inputs.toArray(new String[0]));

		// Load the output conforms
		List<String> outputs = new LinkedList<String>();
		for (HttpSecurityFlowType<?> output : flowTypes) {
			outputs.add(output.getFlowName());
		}
		outputs.add(HttpSecuritySectionSource.OUTPUT_FAILURE);
		this.outputConform.setConform(this.accessOutputs,
				outputs.toArray(new String[0]));
	}

	/**
	 * Obtains the mapping of {@link HttpSecurityType} input name to
	 * {@link WoofAccessInputModel} name.
	 * 
	 * @return Mapping of {@link HttpSecurityType} input name to
	 *         {@link WoofAccessInputModel} name.
	 */
	public Map<String, String> getInputNameMapping() {
		return this.inputConform.getTargetItemToExistingItemMapping();
	}

	/**
	 * Obtains the mapping of {@link HttpSecurityType} output name to
	 * {@link WoofAccessOutputModel} name.
	 * 
	 * @return Mapping of {@link HttpSecurityType} output name to
	 *         {@link WoofAccessOutputModel} name.
	 */
	public Map<String, String> getOutputNameMapping() {
		return this.outputConform.getTargetItemToExistingItemMapping();
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
		InputHandler<ConformModel> inputHandler = new InputHandler<ConformModel>(
				page, this.inputConform);
		inputHandler.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));

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