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

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;

import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.model.section.FunctionNamespaceModel;

/**
 * {@link IWizardPage} providing the listing of
 * {@link ManagedFunctionSourceInstance}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionSourceListingWizardPage extends WizardPage {

	/**
	 * {@link ManagedFunctionSourceInstance} listing.
	 */
	private final ManagedFunctionSourceInstance[] managedFunctionSourceInstances;

	/**
	 * {@link FunctionNamespaceInstance} being refactored or <code>null</code>
	 * if creating.
	 */
	private final FunctionNamespaceInstance namespaceInstance;

	/**
	 * Listing of {@link ManagedFunctionSource} labels in order of
	 * {@link ManagedFunctionSourceInstance} listing.
	 */
	private final String[] managedFunctionSourceLabels;

	/**
	 * Listing of the {@link ManagedFunctionSource} types in order of
	 * {@link ManagedFunctionSourceInstance} listing.
	 */
	private final String[] managedFunctionSourceTypes;

	/**
	 * List containing the {@link ManagedFunctionSource} labels.
	 */
	private List list;

	/**
	 * Initiate.
	 * 
	 * @param managedFunctionSourceInstances
	 *            Listing of {@link ManagedFunctionSourceInstance}.
	 * @param namespaceInstance
	 *            {@link FunctionNamespaceInstance} of
	 *            {@link FunctionNamespaceModel} being refactored.
	 *            <code>null</code> if creating new
	 *            {@link FunctionNamespaceInstance}.
	 */
	ManagedFunctionSourceListingWizardPage(ManagedFunctionSourceInstance[] managedFunctionSourceInstances,
			FunctionNamespaceInstance namespaceInstance) {
		super("ManagedFunctionSource listing");
		this.managedFunctionSourceInstances = managedFunctionSourceInstances;
		this.namespaceInstance = namespaceInstance;

		// Create the listing of labels and types
		this.managedFunctionSourceLabels = new String[this.managedFunctionSourceInstances.length];
		this.managedFunctionSourceTypes = new String[this.managedFunctionSourceInstances.length];
		for (int i = 0; i < this.managedFunctionSourceLabels.length; i++) {
			this.managedFunctionSourceLabels[i] = this.managedFunctionSourceInstances[i]
					.getManagedFunctionSourceLabel();
			this.managedFunctionSourceTypes[i] = this.managedFunctionSourceInstances[i]
					.getManagedFunctionSourceClassName();
		}

		// Specify page details
		this.setTitle("Select a " + ManagedFunctionSource.class.getSimpleName());
	}

	/**
	 * Obtains the selected {@link ManagedFunctionSourceInstance}.
	 * 
	 * @return Selected {@link ManagedFunctionSourceInstance} or
	 *         <code>null</code> if not selected.
	 */
	public ManagedFunctionSourceInstance getSelectedManagedFunctionSourceInstance() {
		int selectedIndex = this.list.getSelectionIndex();
		if (selectedIndex < 0) {
			// No selected managed function source instance
			return null;
		} else {
			// Return the selected managed function source instance
			return this.managedFunctionSourceInstances[selectedIndex];
		}
	}

	/*
	 * ==================== IDialogPage ======================================
	 */

	@Override
	public void createControl(Composite parent) {

		// Fill the width
		Composite page = new Composite(parent, SWT.NULL);
		page.setLayout(new FillLayout(SWT.VERTICAL));

		// Add listing
		this.list = new List(page, SWT.SINGLE | SWT.BORDER);
		this.list.setItems(this.managedFunctionSourceLabels);

		// Add label for selected ManagedFunctionSource
		final Label detail = new Label(page, SWT.NONE);

		// Handle change in selection of ManagedFunctionSource
		this.list.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				// Obtain the selected index
				int selectedIndex = ManagedFunctionSourceListingWizardPage.this.list.getSelectionIndex();

				// Determine if selected
				boolean isSelected = (selectedIndex >= 0);

				// Page complete when work loader selected
				ManagedFunctionSourceListingWizardPage.this.setPageComplete(isSelected);

				// Obtain details
				String managedFunctionSourceClassName = (isSelected
						? ManagedFunctionSourceListingWizardPage.this.managedFunctionSourceInstances[selectedIndex]
								.getManagedFunctionSourceClassName()
						: "");
				detail.setText(managedFunctionSourceClassName);
			}
		});

		// Flag selected namespace instance (if refactoring namespace)
		if (this.namespaceInstance != null) {
			for (int i = 0; i < this.managedFunctionSourceInstances.length; i++) {
				if (this.managedFunctionSourceInstances[i].getManagedFunctionSourceClassName()
						.equals(this.namespaceInstance.getManagedFunctionSourceClassName())) {
					// Managed Function source for the selected instance
					this.list.select(i);
				}
			}
		}

		// Page complete if selected a managed function source
		this.setPageComplete(this.getSelectedManagedFunctionSourceInstance() != null);

		// Provide error if no managed function loaders available
		if (this.managedFunctionSourceInstances.length == 0) {
			this.setErrorMessage("No ManagedFunctionSource classes found");
		}

		// Specify the control
		this.setControl(page);
	}

}