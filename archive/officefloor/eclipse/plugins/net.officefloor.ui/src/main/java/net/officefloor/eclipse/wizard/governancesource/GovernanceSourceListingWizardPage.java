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
package net.officefloor.eclipse.wizard.governancesource;

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

import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.frame.api.governance.Governance;

/**
 * {@link IWizardPage} providing the listing of {@link GovernanceSourceInstance}
 * .
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceSourceListingWizardPage extends WizardPage {

	/**
	 * {@link GovernanceSourceInstance} listing.
	 */
	private final GovernanceSourceInstance[] governanceSourceInstances;

	/**
	 * {@link GovernanceInstance} being refactor or <code>null</code> if
	 * creating.
	 */
	private final GovernanceInstance governanceInstance;

	/**
	 * Listing of {@link GovernanceSource} labels in order of
	 * {@link GovernanceSourceInstance} listing.
	 */
	private final String[] governanceSourceLabels;

	/**
	 * {@link Text} of the {@link Governance} name.
	 */
	private Text governanceName;

	/**
	 * List containing the {@link GovernanceSource} labels.
	 */
	private List list;

	/**
	 * Initiate.
	 * 
	 * @param governanceSourceInstances
	 *            Listing of {@link GovernanceSourceInstance}.
	 * @param governanceInstance
	 *            {@link GovernanceInstance} being refactor or <code>null</code>
	 *            if creating.
	 */
	GovernanceSourceListingWizardPage(GovernanceSourceInstance[] governanceSourceInstances,
			GovernanceInstance governanceInstance) {
		super("GovernanceSource listing");
		this.governanceSourceInstances = governanceSourceInstances;
		this.governanceInstance = governanceInstance;

		// Create the listing of labels
		this.governanceSourceLabels = new String[this.governanceSourceInstances.length];
		for (int i = 0; i < this.governanceSourceLabels.length; i++) {
			this.governanceSourceLabels[i] = this.governanceSourceInstances[i].getGovernanceSourceLabel();
		}

		// Specify page details
		this.setTitle("Select a " + GovernanceSource.class.getSimpleName());
	}

	/**
	 * Obtains the selected {@link GovernanceSourceInstance}.
	 * 
	 * @return Selected {@link GovernanceSourceInstance} or <code>null</code> if
	 *         not selected.
	 */
	public GovernanceSourceInstance getSelectedGovernanceSourceInstance() {
		int selectedIndex = this.list.getSelectionIndex();
		if (selectedIndex < 0) {
			// No selected governance source instance
			return null;
		} else {
			// Return the selected governance source instance
			return this.governanceSourceInstances[selectedIndex];
		}
	}

	/*
	 * ==================== IDialogPage ======================================
	 */

	@Override
	public void createControl(Composite parent) {

		Composite page = new Composite(parent, SWT.NONE);
		page.setLayout(new GridLayout(1, true));

		// Add means to specify governance name
		Composite nameComposite = new Composite(page, SWT.NONE);
		nameComposite.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		nameComposite.setLayout(new GridLayout(2, false));
		Label nameLabel = new Label(nameComposite, SWT.NONE);
		nameLabel.setText("Governance name: ");
		this.governanceName = new Text(nameComposite, SWT.BORDER);
		this.governanceName.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		if (this.governanceInstance != null) {
			// Provide governance name if refactoring.
			// (Must be done before modifyier listener)
			this.governanceName.setText(this.governanceInstance.getGovernanceName());
		}
		this.governanceName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				// Flag the name changed
				GovernanceSourceListingWizardPage.this.handleChange();
			}
		});

		// Add listing of governance sources
		this.list = new List(page, SWT.SINGLE | SWT.BORDER);
		this.list.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		this.list.setItems(this.governanceSourceLabels);
		this.list.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				GovernanceSourceListingWizardPage.this.handleChange();
			}
		});

		// Flag selected governance instance (if refactoring work)
		if (this.governanceInstance != null) {
			for (int i = 0; i < this.governanceSourceInstances.length; i++) {
				if (this.governanceSourceInstances[i].getGovernanceSourceClassName()
						.equals(this.governanceInstance.getGovernanceSourceClassName())) {
					// Governance source for the selected instance
					this.list.select(i);
				}
			}
		}

		// Trigger handle change to set initial state
		this.handleChange();

		// Provide error if no governance loaders available
		if (this.governanceSourceInstances.length == 0) {
			this.setErrorMessage("No GovernanceSource classes found");
		}

		// Specify the control
		this.setControl(page);
	}

	/**
	 * Handles a change.
	 */
	protected void handleChange() {

		// Ensure have governance name
		String name = this.governanceName.getText();
		if (EclipseUtil.isBlank(name)) {
			this.setErrorMessage("Must specify name of governance");
			this.setPageComplete(false);
			return;
		}

		// Determine if governance source selected
		int selectionIndex = this.list.getSelectionIndex();
		if (selectionIndex < 0) {
			this.setErrorMessage("Must select GovernanceSource");
			this.setPageComplete(false);
			return;
		}

		// Specify name for governance source and is complete
		this.governanceSourceInstances[selectionIndex].setGovernanceName(name);
		this.setErrorMessage(null);
		this.setPageComplete(true);
	}

}