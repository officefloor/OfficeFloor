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
package net.officefloor.eclipse.wizard.managedfunctionsource;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.conform.ConformInput;
import net.officefloor.model.conform.ConformModel;
import net.officefloor.model.section.FunctionNamespaceModel;
import net.officefloor.model.section.ManagedFunctionModel;

/**
 * {@link IWizardPage} to align {@link ManagedFunctionModel} instances of the
 * {@link FunctionNamespaceModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionSourceAlignFunctionsWizardPage extends WizardPage {

	/**
	 * {@link FunctionNamespaceInstance} being refactored.
	 */
	private final FunctionNamespaceInstance namespaceInstance;

	/**
	 * {@link ConformInput}.
	 */
	private final ConformInput input;

	/**
	 * Conforms the {@link FunctionNamespaceModel}.
	 * 
	 * @param namespaceInstance
	 *            {@link FunctionNamespaceInstance} being refactored.
	 */
	public ManagedFunctionSourceAlignFunctionsWizardPage(FunctionNamespaceInstance namespaceInstance) {
		super("Refactor Managed Functions");
		this.namespaceInstance = namespaceInstance;
		this.setTitle("Refactor functions of namespace");
		this.input = new ConformInput();
	}

	/**
	 * Loads the {@link ManagedFunctionSourceInstance}.
	 * 
	 * @param managedFunctionSourceInstance
	 *            {@link ManagedFunctionSourceInstance} of the selected
	 *            {@link ManagedFunctionSource}.
	 */
	public void loadWorkSourceInstance(ManagedFunctionSourceInstance managedFunctionSourceInstance) {

		// Obtain the listing of managed functions
		List<String> managedFunctionNames = new LinkedList<String>();
		for (ManagedFunctionModel managedFunctionModel : this.namespaceInstance.getFunctionNamespaceModel()
				.getManagedFunctions()) {
			managedFunctionNames.add(managedFunctionModel.getManagedFunctionName());
		}

		// Obtain the listing of function types
		List<String> functionTypeNames = new LinkedList<String>();
		for (ManagedFunctionType<?, ?> functionType : managedFunctionSourceInstance.getFunctionNamespaceType()
				.getManagedFunctionTypes()) {
			functionTypeNames.add(functionType.getFunctionName());
		}

		// Provide specify functions to conform
		this.input.setConform(managedFunctionNames.toArray(new String[0]), functionTypeNames.toArray(new String[0]));
	}

	/**
	 * Obtains the mapping of {@link ManagedFunctionType} name to
	 * {@link ManagedFunctionModel} name.
	 * 
	 * @return Mapping of {@link ManagedFunctionType} name to
	 *         {@link ManagedFunctionModel} name.
	 */
	public Map<String, String> getManagedFunctionNameMapping() {
		return this.input.getTargetItemToExistingItemMapping();
	}

	/*
	 * ================== IDialogPage =========================================
	 */

	@Override
	public void createControl(Composite parent) {
		// Create the composite to contain the graphical viewer
		Composite page = new Composite(parent, SWT.NONE);
		page.setLayout(new GridLayout(1, false));

		// Create input to conform the namespace
		InputHandler<ConformModel> inputHandler = new InputHandler<ConformModel>(page, this.input);
		inputHandler.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Specify the control
		this.setControl(page);
	}

}