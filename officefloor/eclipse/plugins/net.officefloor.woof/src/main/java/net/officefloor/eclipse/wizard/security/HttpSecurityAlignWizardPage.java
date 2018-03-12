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
package net.officefloor.eclipse.wizard.security;

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

import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.conform.ConformInput;
import net.officefloor.model.conform.ConformModel;
import net.officefloor.web.security.type.HttpSecurityFlowType;
import net.officefloor.web.security.type.HttpSecurityType;
import net.officefloor.woof.model.woof.WoofSecurityModel;
import net.officefloor.woof.model.woof.WoofSecurityOutputModel;

/**
 * {@link IWizardPage} to align refactoring of {@link SecurityInstance}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityAlignWizardPage extends WizardPage {

	/**
	 * {@link SecurityInstance}.
	 */
	private final SecurityInstance securityInstance;

	/**
	 * {@link ConformInput} for outputs.
	 */
	private final ConformInput outputConform = new ConformInput();

	/**
	 * {@link SecurityInstance} outputs.
	 */
	private final String[] securityOutputs;

	/**
	 * Initiate.
	 * 
	 * @param securityInstance
	 *            {@link SecurityInstance}.
	 */
	protected HttpSecurityAlignWizardPage(SecurityInstance securityInstance) {
		super("Refactor Security");
		this.securityInstance = securityInstance;
		this.setTitle("Refactor Security");

		// Create the outputs from security
		WoofSecurityModel model = this.securityInstance.getWoofSecurityModel();
		List<String> outputs = new LinkedList<String>();
		for (WoofSecurityOutputModel output : model.getOutputs()) {
			outputs.add(output.getWoofSecurityOutputName());
		}
		this.securityOutputs = outputs.toArray(new String[0]);
	}

	/**
	 * Loads the {@link HttpSecuritySourceInstance}.
	 * 
	 * @param instance
	 *            {@link HttpSecuritySourceInstance}.
	 */
	public void loadHttpSecuritySourceInstance(HttpSecuritySourceInstance instance) {

		// Ensure obtain HTTP Security type (may still require properties)
		HttpSecurityType<?, ?, ?, ?, ?> httpSecurityType = instance.getHttpSecurityType();
		if (httpSecurityType == null) {
			return;
		}

		// Obtain the output flows
		HttpSecurityFlowType<?>[] flowTypes = httpSecurityType.getFlowTypes();

		// Load the output conforms
		List<String> outputs = new LinkedList<String>();
		for (HttpSecurityFlowType<?> output : flowTypes) {
			outputs.add(output.getFlowName());
		}
		this.outputConform.setConform(this.securityOutputs, outputs.toArray(new String[0]));
	}

	/**
	 * Obtains the mapping of {@link HttpSecurityType} output name to
	 * {@link WoofSecurityOutputModel} name.
	 * 
	 * @return Mapping of {@link HttpSecurityType} output name to
	 *         {@link WoofSecurityOutputModel} name.
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

		// Create conform for outputs
		new Label(page, SWT.NONE).setText("Outputs");
		InputHandler<ConformModel> outputHandler = new InputHandler<ConformModel>(page, this.outputConform);
		outputHandler.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Specify the control
		this.setControl(page);
	}

}