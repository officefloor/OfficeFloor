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
package net.officefloor.eclipse.wizard.administrationsource;

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
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

import net.officefloor.compile.spi.administration.source.AdministrationSource;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.frame.api.administration.Administration;

/**
 * {@link IWizardPage} providing the listing of
 * {@link AdministrationSourceInstance}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministrationSourceListingWizardPage extends WizardPage {

	/**
	 * {@link AdministrationSourceInstance} listing.
	 */
	private final AdministrationSourceInstance[] administrationSourceInstances;

	/**
	 * Listing of {@link AdministrationSource} labels in order of
	 * {@link AdministrationSourceInstance} listing.
	 */
	private final String[] administrationSourceLabels;

	/**
	 * {@link Text} of the {@link Administration} name.
	 */
	private Text administrationName;

	/**
	 * List containing the {@link AdministrationSource} labels.
	 */
	private List administrationLabels;

	/**
	 * Initiate.
	 * 
	 * @param administrationSourceInstances
	 *            Listing of {@link AdministrationSourceInstance}.
	 */
	AdministrationSourceListingWizardPage(AdministrationSourceInstance[] administrationSourceInstances) {
		super("AdministrationSource listing");
		this.administrationSourceInstances = administrationSourceInstances;

		// Create the listing of labels
		this.administrationSourceLabels = new String[this.administrationSourceInstances.length];
		for (int i = 0; i < this.administrationSourceLabels.length; i++) {
			this.administrationSourceLabels[i] = this.administrationSourceInstances[i].getAdministrationSourceLabel();
		}

		// Specify page details
		this.setTitle("Select a " + AdministrationSource.class.getSimpleName());
	}

	/**
	 * Obtains the selected {@link AdministrationSourceInstance}.
	 * 
	 * @return Selected {@link AdministrationSourceInstance} or
	 *         <code>null</code> if not selected.
	 */
	public AdministrationSourceInstance getSelectedAdministrationSourceInstance() {
		int selectedIndex = this.administrationLabels.getSelectionIndex();
		if (selectedIndex < 0) {
			// No selected administration source instance
			return null;
		} else {
			// Return the selected administration source instance
			return this.administrationSourceInstances[selectedIndex];
		}
	}

	/*
	 * ==================== IDialogPage ======================================
	 */

	@Override
	public void createControl(Composite parent) {

		Composite page = new Composite(parent, SWT.NONE);
		page.setLayout(new GridLayout(1, true));

		// Add means to specify administration name
		Composite nameComposite = new Composite(page, SWT.NONE);
		nameComposite.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		nameComposite.setLayout(new GridLayout(2, false));
		Label nameLabel = new Label(nameComposite, SWT.NONE);
		nameLabel.setText("Administrator name: ");
		this.administrationName = new Text(nameComposite, SWT.BORDER);
		this.administrationName.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		this.administrationName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				// Flag the name changed
				AdministrationSourceListingWizardPage.this.handleChange();
			}
		});

		// Add listing of administration sources
		this.administrationLabels = new List(page, SWT.SINGLE | SWT.BORDER);
		this.administrationLabels.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		this.administrationLabels.setItems(this.administrationSourceLabels);
		this.administrationLabels.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AdministrationSourceListingWizardPage.this.handleChange();
			}
		});

		// Initially page not complete (as must select administration source)
		this.setPageComplete(false);

		// Provide error if no administration loaders available
		if (this.administrationSourceInstances.length == 0) {
			this.setErrorMessage("No AdministrationSource classes found");
		}

		// Specify the control
		this.setControl(page);
	}

	/**
	 * Handles a change.
	 */
	protected void handleChange() {

		// Ensure have administrator name
		String name = this.administrationName.getText();
		if (EclipseUtil.isBlank(name)) {
			this.setErrorMessage("Must specify name of administration");
			this.setPageComplete(false);
			return;
		}

		// Determine if administration source selected
		int selectionIndex = this.administrationLabels.getSelectionIndex();
		if (selectionIndex < 0) {
			this.setErrorMessage("Must select AdministrationSource");
			this.setPageComplete(false);
			return;
		}

		// Specify name for administration source and is complete
		this.administrationSourceInstances[selectionIndex].setAdministrationName(name);
		this.setErrorMessage(null);
		this.setPageComplete(true);
	}

}