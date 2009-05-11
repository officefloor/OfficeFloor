/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.eclipse.wizard.administratorsource;

import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.source.AdministratorSource;

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

/**
 * {@link IWizardPage} providing the listing of
 * {@link AdministratorSourceInstance}.
 * 
 * @author Daniel
 */
public class AdministratorSourceListingWizardPage extends WizardPage {

	/**
	 * {@link AdministratorSourceInstance} listing.
	 */
	private final AdministratorSourceInstance[] administratorSourceInstances;

	/**
	 * Listing of {@link AdministratorSource} labels in order of
	 * {@link AdministratorSourceInstance} listing.
	 */
	private final String[] administratorSourceLabels;

	/**
	 * {@link Text} of the {@link Administrator} name.
	 */
	private Text administratorName;

	/**
	 * List containing the {@link AdministratorSource} labels.
	 */
	private List list;

	/**
	 * Initiate.
	 * 
	 * @param administratorSourceInstances
	 *            Listing of {@link AdministratorSourceInstance}.
	 */
	AdministratorSourceListingWizardPage(
			AdministratorSourceInstance[] administratorSourceInstances) {
		super("AdministratorSource listing");
		this.administratorSourceInstances = administratorSourceInstances;

		// Create the listing of labels
		this.administratorSourceLabels = new String[this.administratorSourceInstances.length];
		for (int i = 0; i < this.administratorSourceLabels.length; i++) {
			this.administratorSourceLabels[i] = this.administratorSourceInstances[i]
					.getAdministratorSourceLabel();
		}

		// Specify page details
		this.setTitle("Select a " + AdministratorSource.class.getSimpleName());
	}

	/**
	 * Obtains the selected {@link AdministratorSourceInstance}.
	 * 
	 * @return Selected {@link AdministratorSourceInstance} or <code>null</code>
	 *         if not selected.
	 */
	public AdministratorSourceInstance getSelectedAdministratorSourceInstance() {
		int selectedIndex = this.list.getSelectionIndex();
		if (selectedIndex < 0) {
			// No selected administrator source instance
			return null;
		} else {
			// Return the selected administrator source instance
			return this.administratorSourceInstances[selectedIndex];
		}
	}

	/*
	 * ==================== IDialogPage ======================================
	 */

	@Override
	public void createControl(Composite parent) {

		Composite page = new Composite(parent, SWT.NONE);
		page.setLayout(new GridLayout(1, true));

		// Add means to specify administrator name
		Composite nameComposite = new Composite(page, SWT.NONE);
		nameComposite.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true,
				false));
		nameComposite.setLayout(new GridLayout(2, false));
		Label nameLabel = new Label(nameComposite, SWT.NONE);
		nameLabel.setText("Administrator name: ");
		this.administratorName = new Text(nameComposite, SWT.BORDER);
		this.administratorName.setLayoutData(new GridData(SWT.FILL,
				SWT.BEGINNING, true, false));
		this.administratorName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				// Flag the name changed
				AdministratorSourceListingWizardPage.this.handleChange();
			}
		});

		// Add listing of administrator sources
		this.list = new List(page, SWT.SINGLE | SWT.BORDER);
		this.list.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true,
				false));
		this.list.setItems(this.administratorSourceLabels);
		this.list.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AdministratorSourceListingWizardPage.this.handleChange();
			}
		});

		// Initially page not complete (as must select administrator source)
		this.setPageComplete(false);

		// Provide error if no administrator loaders available
		if (this.administratorSourceInstances.length == 0) {
			this.setErrorMessage("No AdministratorSource classes found");
		}

		// Specify the control
		this.setControl(page);
	}

	/**
	 * Handles a change.
	 */
	protected void handleChange() {

		// Ensure have administrator name
		String name = this.administratorName.getText();
		if (EclipseUtil.isBlank(name)) {
			this.setErrorMessage("Must specify name of administrator");
			this.setPageComplete(false);
			return;
		}

		// Determine if administrator source selected
		int selectionIndex = this.list.getSelectionIndex();
		if (selectionIndex < 0) {
			this.setErrorMessage("Must select AdministratorSource");
			this.setPageComplete(false);
			return;
		}

		// Specify name for administrator source and is complete
		this.administratorSourceInstances[selectionIndex]
				.setAdministratorName(name);
		this.setErrorMessage(null);
		this.setPageComplete(true);
	}

}