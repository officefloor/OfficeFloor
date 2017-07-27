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

import java.util.HashMap;
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
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import net.officefloor.compile.managedfunction.ManagedFunctionEscalationType;
import net.officefloor.compile.managedfunction.ManagedFunctionFlowType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.conform.ConformInput;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.model.conform.ConformModel;
import net.officefloor.model.section.FunctionEscalationModel;
import net.officefloor.model.section.FunctionFlowModel;
import net.officefloor.model.section.FunctionModel;
import net.officefloor.model.section.FunctionNamespaceModel;
import net.officefloor.model.section.ManagedFunctionModel;
import net.officefloor.model.section.ManagedFunctionToFunctionModel;

/**
 * {@link IWizardPage} to align {@link FunctionFlowModel} and
 * {@link FunctionEscalationModel} instances of the {@link FunctionModel}
 * instances for the {@link FunctionNamespaceModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionSourceAlignFlowsEscalationsWizardPage extends WizardPage {

	/**
	 * {@link FunctionNamespaceInstance} being refactored.
	 */
	private final FunctionNamespaceInstance namespaceInstance;

	/**
	 * Page.
	 */
	private Composite page;

	/**
	 * {@link TabFolder} providing a {@link TabItem} for each
	 * {@link FunctionModel}.
	 */
	private TabFolder tabFolder;

	/**
	 * Mapping of {@link FunctionModel} name to its {@link ConformInput} for the
	 * {@link FunctionFlowModel} instances.
	 */
	private Map<String, ConformInput> functionFlowConforms = new HashMap<String, ConformInput>();

	/**
	 * Mapping of {@link FunctionModel} name to its {@link ConformInput} for the
	 * {@link FunctionEscalationModel} instances.
	 */
	private Map<String, ConformInput> functionEscalationConforms = new HashMap<String, ConformInput>();

	/**
	 * Conforms the {@link FunctionNamespaceInstance}.
	 * 
	 * @param namespaceInstance
	 *            {@link FunctionNamespaceInstance} being refactored.
	 */
	public ManagedFunctionSourceAlignFlowsEscalationsWizardPage(FunctionNamespaceInstance namespaceInstance) {
		super("Refactor Function Flows and Escalations");
		this.namespaceInstance = namespaceInstance;
		this.setTitle("Refactor flows and escalations of functions");
	}

	/**
	 * Loads the {@link ManagedFunctionSourceInstance} and mapping of
	 * {@link ManagedFunctionType} name to {@link ManagedFunctionModel} name.
	 * 
	 * @param managedFunctionNameMapping
	 *            Mapping of {@link ManagedFunctionType} name to
	 *            {@link ManagedFunctionModel} name.
	 * @param managedFunctionSourceInstance
	 *            {@link ManagedFunctionSourceInstance} of the selected
	 *            {@link ManagedFunctionSource}.
	 */
	public void loadWorkTaskMappingAndWorkSourceInstance(Map<String, String> managedFunctionNameMapping,
			ManagedFunctionSourceInstance managedFunctionSourceInstance) {

		// Clear page to load new details
		if (this.tabFolder != null) {
			this.tabFolder.dispose();
			this.tabFolder = null;
			this.functionFlowConforms.clear();
			this.functionEscalationConforms.clear();
		}

		// Add the tab folder for function types
		this.tabFolder = new TabFolder(this.page, SWT.NONE);
		this.tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		for (ManagedFunctionType<?, ?> functionType : managedFunctionSourceInstance.getFunctionNamespaceType()
				.getManagedFunctionTypes()) {

			// Obtain the corresponding functions for the function type
			List<FunctionModel> functions = new LinkedList<>();
			String functionName = functionType.getFunctionName();
			String managedFunctionName = managedFunctionNameMapping.get(functionName);
			ManagedFunctionModel managedFunction = null;
			if (!(EclipseUtil.isBlank(managedFunctionName))) {
				for (ManagedFunctionModel checkFunction : this.namespaceInstance.getFunctionNamespaceModel()
						.getManagedFunctions()) {
					if (managedFunctionName.equals(checkFunction.getManagedFunctionName())) {
						managedFunction = checkFunction;
					}
				}
			}
			if (managedFunction != null) {
				for (ManagedFunctionToFunctionModel conn : managedFunction.getFunctions()) {
					FunctionModel function = conn.getFunction();
					if (function != null) {
						functions.add(function);
					}
				}
			}
			if (functions.size() == 0) {
				continue; // no functions for managed function
			}

			// Obtain the list of flows for function type
			List<String> flowTypeNames = new LinkedList<String>();
			for (ManagedFunctionFlowType<?> flowType : functionType.getFlowTypes()) {
				flowTypeNames.add(flowType.getFlowName());
			}

			// Obtain the list of escalations for function type
			List<String> escalationTypes = new LinkedList<String>();
			for (ManagedFunctionEscalationType escalationType : functionType.getEscalationTypes()) {
				escalationTypes.add(escalationType.getEscalationType().getName());
			}

			// Add tab for each of the functions
			for (FunctionModel function : functions) {

				// Add the tab for the function
				TabItem tabItem = new TabItem(this.tabFolder, SWT.NONE);
				tabItem.setText(function.getFunctionName());

				// Create the composite to contain both flows and escalations
				Composite tabContents = new Composite(this.tabFolder, SWT.NONE);
				tabContents.setLayout(new GridLayout(1, false));

				// Obtain the flow names for the function
				List<String> flowNames = new LinkedList<String>();
				for (FunctionFlowModel flow : function.getFunctionFlows()) {
					flowNames.add(flow.getFlowName());
				}

				// Add the conform of flows for function
				new Label(tabContents, SWT.NONE).setText("Flows");
				ConformInput flowInput = new ConformInput();
				flowInput.setConform(flowNames.toArray(new String[0]), flowTypeNames.toArray(new String[0]));
				InputHandler<ConformModel> flowHandler = new InputHandler<ConformModel>(tabContents, flowInput);
				flowHandler.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				this.functionFlowConforms.put(function.getFunctionName(), flowInput);

				// Obtain the escalation types for function
				List<String> escalationTypeNames = new LinkedList<String>();
				for (FunctionEscalationModel escalation : function.getFunctionEscalations()) {
					escalationTypeNames.add(escalation.getEscalationType());
				}

				// Add the conform of escalations for function
				new Label(tabContents, SWT.NONE).setText("Escalations");
				ConformInput escalationInput = new ConformInput();
				escalationInput.setConform(escalationTypeNames.toArray(new String[0]),
						escalationTypes.toArray(new String[0]));
				InputHandler<ConformModel> escalationHandler = new InputHandler<ConformModel>(tabContents,
						escalationInput);
				escalationHandler.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				this.functionEscalationConforms.put(function.getFunctionName(), escalationInput);

				// Specify tab contents
				tabItem.setControl(tabContents);
			}
		}

		// Refresh the page
		this.page.layout(true);
	}

	/**
	 * Obtains the mapping of {@link ManagedFunctionFlowType} name to
	 * {@link FunctionFlowModel} name for a particular {@link FunctionModel}
	 * name.
	 * 
	 * @return Mapping of {@link ManagedFunctionFlowType} name to
	 *         {@link FunctionFlowModel} name for a particular
	 *         {@link FunctionModel} name.
	 */
	public Map<String, Map<String, String>> getFunctionFlowNameMappingForFunction() {
		// Create the map
		Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
		for (String key : this.functionFlowConforms.keySet()) {
			ConformInput input = this.functionFlowConforms.get(key);
			map.put(key, input.getTargetItemToExistingItemMapping());
		}

		// Return the map
		return map;
	}

	/**
	 * Obtains the mapping of {@link ManagedFunctionEscalationType} type to
	 * {@link FunctionEscalationModel} type for a particular
	 * {@link FunctionModel} name.
	 * 
	 * @return Mapping of {@link ManagedFunctionFlowType} name to
	 *         {@link FunctionEscalationModel} name for a particular
	 *         {@link FunctionModel} name.
	 */
	public Map<String, Map<String, String>> getFunctionEscalationTypeMappingForFunction() {
		// Create the map
		Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
		for (String key : this.functionEscalationConforms.keySet()) {
			ConformInput input = this.functionEscalationConforms.get(key);
			map.put(key, input.getTargetItemToExistingItemMapping());
		}

		// Return the map
		return map;
	}

	/*
	 * ================== IDialogPage =========================================
	 */

	@Override
	public void createControl(Composite parent) {
		// Create the composite to contain the graphical viewer
		this.page = new Composite(parent, SWT.NONE);
		this.page.setLayout(new GridLayout(1, false));

		// Controls of page loaded when managed function mapping loaded

		// Specify the control
		this.setControl(this.page);
	}

}