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
package net.officefloor.eclipse.wizard.worksource;

import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.frame.api.execute.Work;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;

/**
 * {@link IWizardPage} providing the listing of {@link WorkSourceInstance}.
 * 
 * @author Daniel Sagenschneider
 */
public class WorkSourceListingWizardPage extends WizardPage {

	/**
	 * {@link WorkSourceInstance} listing.
	 */
	private final WorkSourceInstance[] workSourceInstances;

	/**
	 * {@link WorkInstance} being refactored or <code>null</code> if creating.
	 */
	private final WorkInstance workInstance;

	/**
	 * Listing of {@link WorkSource} labels in order of
	 * {@link WorkSourceInstance} listing.
	 */
	private final String[] workSourceLabels;

	/**
	 * Listing of the {@link WorkSource} types in order of
	 * {@link WorkSourceInstance} listing.
	 */
	private final String[] workSourceTypes;

	/**
	 * List containing the {@link WorkSource} labels.
	 */
	private List list;

	/**
	 * Initiate.
	 * 
	 * @param workSourceInstances
	 *            Listing of {@link WorkSourceInstance}.
	 * @param workInstance
	 *            {@link WorkInstance} of {@link Work} being refactored.
	 *            <code>null</code> if creating new {@link WorkInstance}.
	 */
	WorkSourceListingWizardPage(WorkSourceInstance[] workSourceInstances,
			WorkInstance workInstance) {
		super("WorkSource listing");
		this.workSourceInstances = workSourceInstances;
		this.workInstance = workInstance;

		// Create the listing of labels and types
		this.workSourceLabels = new String[this.workSourceInstances.length];
		this.workSourceTypes = new String[this.workSourceInstances.length];
		for (int i = 0; i < this.workSourceLabels.length; i++) {
			this.workSourceLabels[i] = this.workSourceInstances[i]
					.getWorkSourceLabel();
			this.workSourceTypes[i] = this.workSourceInstances[i]
					.getWorkSourceClassName();
		}

		// Specify page details
		this.setTitle("Select a " + WorkSource.class.getSimpleName());
	}

	/**
	 * Obtains the selected {@link WorkSourceInstance}.
	 * 
	 * @return Selected {@link WorkSourceInstance} or <code>null</code> if not
	 *         selected.
	 */
	public WorkSourceInstance getSelectedWorkSourceInstance() {
		int selectedIndex = this.list.getSelectionIndex();
		if (selectedIndex < 0) {
			// No selected work source instance
			return null;
		} else {
			// Return the selected work source instance
			return this.workSourceInstances[selectedIndex];
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
		this.list.setItems(this.workSourceLabels);

		// Add label for selected WorkSource
		final Label detail = new Label(page, SWT.NONE);

		// Handle change in selection of WorkSource
		this.list.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				// Obtain the selected index
				int selectedIndex = WorkSourceListingWizardPage.this.list
						.getSelectionIndex();

				// Determine if selected
				boolean isSelected = (selectedIndex >= 0);

				// Page complete when work loader selected
				WorkSourceListingWizardPage.this.setPageComplete(isSelected);

				// Obtain details
				String workSourceClassName = (isSelected ? WorkSourceListingWizardPage.this.workSourceInstances[selectedIndex]
						.getWorkSourceClassName()
						: "");
				detail.setText(workSourceClassName);
			}
		});

		// Flag selected work instance (if refactoring work)
		if (this.workInstance != null) {
			for (int i = 0; i < this.workSourceInstances.length; i++) {
				if (this.workSourceInstances[i].getWorkSourceClassName()
						.equals(this.workInstance.getWorkSourceClassName())) {
					// Work source for the selected instance
					this.list.select(i);
				}
			}
		}

		// Page complete if selected a work source
		this.setPageComplete(this.getSelectedWorkSourceInstance() != null);

		// Provide error if no work loaders available
		if (this.workSourceInstances.length == 0) {
			this.setErrorMessage("No WorkSource classes found");
		}

		// Specify the control
		this.setControl(page);
	}

}