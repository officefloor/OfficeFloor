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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.model.section.FunctionNamespaceModel;

/**
 * {@link IWizardPage} to select the {@link ManagedFunctionType} instances of
 * the {@link FunctionNamespaceType} to include.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionSourceFunctionsWizardPage extends WizardPage {

	/**
	 * Display to obtain the {@link FunctionNamespaceModel} name.
	 */
	private Text namespaceName;

	/**
	 * Display of the {@link ManagedFunctionType} instances.
	 */
	private Table functions;

	/**
	 * {@link ManagedFunctionSourceInstance}.
	 */
	private ManagedFunctionSourceInstance managedFunctionSourceInstance;

	/**
	 * {@link ManagedFunctionType} instances by their names.
	 */
	private Map<String, ManagedFunctionType<?, ?>> functionTypes = null;

	/**
	 * Initiate.
	 */
	protected ManagedFunctionSourceFunctionsWizardPage() {
		super("ManagedFunctionSource functions");

		// Specify page details
		this.setTitle("Select functions");
		this.setPageComplete(false);
	}

	/**
	 * Specifies the {@link ManagedFunctionSourceInstance}.
	 * 
	 * @param managedFunctionSourceInstance
	 *            {@link ManagedFunctionSourceInstance}.
	 */
	public void loadManagedFunctionSourceInstance(ManagedFunctionSourceInstance managedFunctionSourceInstance) {

		// Specify managed function source instance and obtain namespace type
		this.managedFunctionSourceInstance = managedFunctionSourceInstance;
		FunctionNamespaceType namespaceType = (this.managedFunctionSourceInstance != null
				? this.managedFunctionSourceInstance.getFunctionNamespaceType() : null);
		String suggestedNamespaceName = (this.managedFunctionSourceInstance != null
				? this.managedFunctionSourceInstance.getSuggestedFunctionNamespaceName() : "");

		// Specify the suggested namespace name
		this.namespaceName.setText(suggestedNamespaceName);

		// Create the list of function types
		String[] functionTypeNames;
		if (namespaceType == null) {
			// No namespace type, no functions
			functionTypeNames = new String[0];
			this.functionTypes = null;

		} else {
			// Create the listing of the function type names
			ManagedFunctionType<?, ?>[] functionTypes = namespaceType.getManagedFunctionTypes();
			functionTypeNames = new String[functionTypes.length];
			this.functionTypes = new HashMap<String, ManagedFunctionType<?, ?>>(functionTypes.length);
			for (int i = 0; i < functionTypeNames.length; i++) {
				ManagedFunctionType<?, ?> functionType = functionTypes[i];

				// Specify function type name and register function type
				String functionTypeName = functionType.getFunctionName();
				functionTypeNames[i] = functionTypeName;
				this.functionTypes.put(functionTypeName, functionType);
			}
		}

		// Load the function types to choose from (by default all chosen)
		this.functions.removeAll();
		for (String functionTypeName : functionTypeNames) {
			TableItem item = new TableItem(this.functions, SWT.LEFT);
			item.setText(functionTypeName);
			item.setChecked(true);
		}

		// Initiate state
		this.handlePageChange();
	}

	/**
	 * Obtains the name of the {@link FunctionNamespaceModel}.
	 * 
	 * @return Name of the {@link FunctionNamespaceModel}.
	 */
	public String getFunctionNamespaceName() {
		return this.namespaceName.getText();
	}

	/**
	 * Obtains the selected {@link ManagedFunctionType} instances.
	 * 
	 * @return Selected {@link ManagedFunctionType} instances.
	 */
	public List<ManagedFunctionType<?, ?>> getSelectedManagedFunctionTypes() {

		// Ensure have function types registered
		if (this.functionTypes == null) {
			return new ArrayList<ManagedFunctionType<?, ?>>(0);
		}

		// Obtain the listing of checked rows
		List<TableItem> checkedRows = new LinkedList<TableItem>();
		for (TableItem row : this.functions.getItems()) {
			if (row.getChecked()) {
				checkedRows.add(row);
			}
		}

		// Obtain the subsequent function types
		List<ManagedFunctionType<?, ?>> chosenFunctionTypes = new ArrayList<ManagedFunctionType<?, ?>>(
				checkedRows.size());
		for (TableItem checkedRow : checkedRows) {

			// Obtain the chosen function type name
			String functionTypeName = checkedRow.getText();

			// Obtain and add the selected function type
			ManagedFunctionType<?, ?> functionType = this.functionTypes.get(functionTypeName);
			if (functionType != null) {
				chosenFunctionTypes.add(functionType);
			}
		}

		// Return the chosen function types
		return chosenFunctionTypes;
	}

	@Override
	public void createControl(Composite parent) {

		// Create the page for the managed function source
		Composite page = new Composite(parent, SWT.NONE);
		page.setLayout(new GridLayout(1, false));

		// Provide control to specify name
		Composite name = new Composite(page, SWT.NONE);
		name.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		name.setLayout(new GridLayout(2, false));
		new Label(name, SWT.None).setText("Namespace: ");
		this.namespaceName = new Text(name, SWT.SINGLE | SWT.BORDER);
		this.namespaceName.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		this.namespaceName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				ManagedFunctionSourceFunctionsWizardPage.this.handlePageChange();
			}
		});

		// Provide control to select functions
		this.functions = new Table(page, SWT.CHECK);
		this.functions.setHeaderVisible(false);
		this.functions.setLinesVisible(false);
		this.functions.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		this.functions.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				// Ignore everything except check box changes
				if (e.detail != SWT.CHECK) {
					return;
				}

				// Handle check box change
				ManagedFunctionSourceFunctionsWizardPage.this.handlePageChange();
			}
		});

		// Initiate state (currently no managed function source instance)
		this.loadManagedFunctionSourceInstance(null);
		this.handlePageChange();

		// Specify control
		this.setControl(page);
	}

	/**
	 * Handles changes to the page.
	 */
	private void handlePageChange() {

		// Ensure namespace has a name
		String namespaceName = this.namespaceName.getText();
		if ((namespaceName == null) || (namespaceName.trim().length() == 0)) {
			this.setErrorMessage("Must provide namespace");
			this.setPageComplete(false);
			return;
		}

		// Ensure that one or more functions are selected
		int selectionCount = this.getSelectedManagedFunctionTypes().size();
		if (selectionCount == 0) {
			this.setErrorMessage("Must select at least one function");
			this.setPageComplete(false);
			return;
		}

		// Make complete
		this.setErrorMessage(null);
		this.setPageComplete(true);
	}
}