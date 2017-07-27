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
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.conform.ConformInput;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.model.conform.ConformModel;
import net.officefloor.model.section.FunctionNamespaceModel;
import net.officefloor.model.section.ManagedFunctionModel;
import net.officefloor.model.section.ManagedFunctionObjectModel;

/**
 * {@link IWizardPage} to align {@link ManagedFunctionObjectModel} instances of
 * the {@link ManagedFunctionModel} instances of the
 * {@link FunctionNamespaceModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionSourceAlignObjectsWizardPage extends WizardPage {

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
	 * {@link ManagedFunctionType}.
	 */
	private TabFolder tabFolder;

	/**
	 * Mapping of {@link ManagedFunctionModel} name its {@link ConformInput}.
	 */
	private Map<String, ConformInput> managedFunctionObjectConforms = new HashMap<String, ConformInput>();

	/**
	 * Conforms the {@link FunctionNamespaceInstance}.
	 * 
	 * @param namespaceInstance
	 *            {@link FunctionNamespaceInstance} being refactored.
	 */
	public ManagedFunctionSourceAlignObjectsWizardPage(FunctionNamespaceInstance namespaceInstance) {
		super("Refactor Managed Function Objects");
		this.namespaceInstance = namespaceInstance;
		this.setTitle("Refactor objects of managed functions");
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
	public void loadManagedFunctionMappingAndManagedFunctionSourceInstance(
			Map<String, String> managedFunctionNameMapping,
			ManagedFunctionSourceInstance managedFunctionSourceInstance) {

		// Clear page to load new details
		if (this.tabFolder != null) {
			this.tabFolder.dispose();
			this.tabFolder = null;
			this.managedFunctionObjectConforms.clear();
		}

		// Add the tab folder for function types
		this.tabFolder = new TabFolder(this.page, SWT.NONE);
		this.tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		for (ManagedFunctionType<?, ?> functionType : managedFunctionSourceInstance.getFunctionNamespaceType()
				.getManagedFunctionTypes()) {

			// Add the tab for the function type
			TabItem tabItem = new TabItem(this.tabFolder, SWT.NONE);
			tabItem.setText(functionType.getFunctionName());

			// Obtain the list of objects for function type
			List<String> objectTypeNames = new LinkedList<String>();
			for (ManagedFunctionObjectType<?> objectType : functionType.getObjectTypes()) {
				objectTypeNames.add(objectType.getObjectName());
			}

			// Obtain the objects for corresponding managed function
			List<String> managedFunctionObjectNames = new LinkedList<String>();
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
				for (ManagedFunctionObjectModel managedFunctionObject : managedFunction.getManagedFunctionObjects()) {
					managedFunctionObjectNames.add(managedFunctionObject.getObjectName());
				}
			}

			// Create the conform input
			ConformInput conformInput = new ConformInput();
			conformInput.setConform(managedFunctionObjectNames.toArray(new String[0]),
					objectTypeNames.toArray(new String[0]));

			// Create input to conform objects of function
			InputHandler<ConformModel> inputHandler = new InputHandler<ConformModel>(this.tabFolder, conformInput);
			tabItem.setControl(inputHandler.getControl());

			// Add conform if managed function
			if (managedFunction != null) {
				this.managedFunctionObjectConforms.put(managedFunctionName, conformInput);
			}
		}

		// Refresh the page
		this.page.layout(true);
	}

	/**
	 * Obtains the mapping of {@link ManagedFunctionObjectType} name to
	 * {@link ManagedFunctionObjectModel} name for a particular
	 * {@link ManagedFunctionModel} name.
	 * 
	 * @return Mapping of {@link ManagedFunctionObjectType} name to
	 *         {@link ManagedFunctionObjectModel} name for a particular
	 *         {@link ManagedFunctionModel} name.
	 */
	public Map<String, Map<String, String>> getManagedFunctionObjectNameMappingForManagedFunction() {
		// Create the map
		Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
		for (String key : this.managedFunctionObjectConforms.keySet()) {
			ConformInput input = this.managedFunctionObjectConforms.get(key);
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