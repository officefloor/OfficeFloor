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
package net.officefloor.eclipse.wizard.managedobjectsource;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * {@link IWizardPage} providing the listing of
 * {@link ManagedObjectSourceInstance}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectSourceListingWizardPage extends WizardPage {

	/**
	 * {@link ManagedObjectSourceInstance} listing.
	 */
	private final ManagedObjectSourceInstance[] managedObjectSourceInstances;

	/**
	 * Listing of {@link ManagedObjectSource} labels in order of
	 * {@link ManagedObjectSourceInstance} listing.
	 */
	private final String[] managedObjectSourceLabels;

	/**
	 * List containing the {@link ManagedObjectSource} labels.
	 */
	private List list;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectSourceInstances
	 *            Listing of {@link ManagedObjectSourceInstance}.
	 */
	ManagedObjectSourceListingWizardPage(ManagedObjectSourceInstance[] managedObjectSourceInstances) {
		super("ManagedObjectSource listing");
		this.managedObjectSourceInstances = managedObjectSourceInstances;

		// Create the listing of labels
		this.managedObjectSourceLabels = new String[this.managedObjectSourceInstances.length];
		for (int i = 0; i < this.managedObjectSourceLabels.length; i++) {
			this.managedObjectSourceLabels[i] = this.managedObjectSourceInstances[i].getManagedObjectSourceLabel();
		}

		// Specify page details
		this.setTitle("Select a " + ManagedObjectSource.class.getSimpleName());
	}

	/**
	 * Obtains the selected {@link ManagedObjectSourceInstance}.
	 * 
	 * @return Selected {@link ManagedObjectSourceInstance} or <code>null</code>
	 *         if not selected.
	 */
	public ManagedObjectSourceInstance getSelectedManagedObjectSourceInstance() {
		int selectedIndex = this.list.getSelectionIndex();
		if (selectedIndex < 0) {
			// No selected managed object source instance
			return null;
		} else {
			// Return the selected managed object source instance
			return this.managedObjectSourceInstances[selectedIndex];
		}
	}

	/*
	 * ==================== IDialogPage ======================================
	 */

	@Override
	public void createControl(Composite parent) {

		// Fill the width
		Composite page = new Composite(parent, SWT.NULL);
		page.setLayout(new FillLayout(SWT.HORIZONTAL));

		// Add listing
		this.list = new List(page, SWT.SINGLE | SWT.BORDER);
		this.list.setItems(this.managedObjectSourceLabels);
		this.list.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Determine if selected
				boolean isSelected = (ManagedObjectSourceListingWizardPage.this.list.getSelectionIndex() >= 0);

				// Page complete when managed object loader selected
				ManagedObjectSourceListingWizardPage.this.setPageComplete(isSelected);
			}
		});

		// Initially page not complete (as must select managed object loader)
		this.setPageComplete(false);

		// Provide error if no managed object loaders available
		if (this.managedObjectSourceInstances.length == 0) {
			this.setErrorMessage("No ManagedObjectSource classes found");
		}

		// Specify the control
		this.setControl(page);
	}

}