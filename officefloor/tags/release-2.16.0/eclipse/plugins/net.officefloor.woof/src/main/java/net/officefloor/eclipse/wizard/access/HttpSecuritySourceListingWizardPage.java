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
package net.officefloor.eclipse.wizard.access;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.plugin.web.http.security.HttpSecuritySource;

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
 * {@link HttpSecuritySourceInstance}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecuritySourceListingWizardPage extends WizardPage implements
		CompilerIssues {

	/**
	 * {@link HttpSecuritySourceInstance} listing.
	 */
	private final HttpSecuritySourceInstance[] httpSecuritySourceInstances;

	/**
	 * Listing of {@link HttpSecuritySource} labels in order of
	 * {@link HttpSecuritySourceInstance} listing.
	 */
	private final String[] httpSecuritySourceLabels;

	/**
	 * {@link AccessInstance} to refactor or <code>null</code> if creating new
	 * {@link AccessInstance}.
	 */
	private final AccessInstance accessInstance;

	/**
	 * List containing the {@link HttpSecuritySource} labels.
	 */
	private List list;

	/**
	 * Selected {@link HttpSecuritySourceInstance}.
	 */
	private HttpSecuritySourceInstance selectedInstance = null;

	/**
	 * {@link Text} of the authentication timeout.
	 */
	private Text authenticationTimeoutText;

	/**
	 * Initiate.
	 * 
	 * @param httpSecuritySourceInstances
	 *            Listing of {@link HttpSecuritySourceInstance}.
	 * @param accessInstance
	 *            {@link AccessInstance} to refactor or <code>null</code> if
	 *            creating new.
	 */
	HttpSecuritySourceListingWizardPage(
			HttpSecuritySourceInstance[] httpSecuritySourceInstances,
			AccessInstance accessInstance) {
		super("SectionSource listing");
		this.httpSecuritySourceInstances = httpSecuritySourceInstances;
		this.accessInstance = accessInstance;

		// Create the listing of labels
		this.httpSecuritySourceLabels = new String[this.httpSecuritySourceInstances.length];
		for (int i = 0; i < this.httpSecuritySourceLabels.length; i++) {
			this.httpSecuritySourceLabels[i] = this.httpSecuritySourceInstances[i]
					.getHttpSecuritySourceLabel();
		}

		// Specify page details
		this.setTitle("Select a " + HttpSecuritySource.class.getSimpleName());
	}

	/**
	 * Obtains the selected {@link HttpSecuritySourceInstance}.
	 * 
	 * @return Selected {@link HttpSecuritySourceInstance} or <code>null</code>
	 *         if not selected.
	 */
	public HttpSecuritySourceInstance getSelectedHttpSecuritySourceInstance() {
		return this.selectedInstance;
	}

	/*
	 * ==================== IDialogPage ======================================
	 */

	@Override
	public void createControl(Composite parent) {

		Composite page = new Composite(parent, SWT.NONE);
		page.setLayout(new GridLayout(1, true));

		// Obtain the initial values
		long initialAuthenticationTimeout = 3000;
		int initialSectionSourceIndex = -1;
		if (this.accessInstance != null) {
			// Have access instance, so provide initial source details
			initialAuthenticationTimeout = this.accessInstance
					.getAuthenticationTimeout();
			String httpSecuritySourceClassName = this.accessInstance
					.getHttpSecuritySourceClassName();
			for (int i = 0; i < this.httpSecuritySourceInstances.length; i++) {
				if (httpSecuritySourceClassName
						.equals(this.httpSecuritySourceInstances[i]
								.getHttpSecuritySourceClassName())) {
					initialSectionSourceIndex = i;
				}
			}
		}

		// Add listing of HTTP Security sources
		this.list = new List(page, SWT.SINGLE | SWT.BORDER);
		this.list.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true,
				false));
		this.list.setItems(this.httpSecuritySourceLabels);
		this.list.setSelection(initialSectionSourceIndex);
		this.list.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				HttpSecuritySourceListingWizardPage.this.handleChange();
			}
		});

		// Add means to specify authentication timeout
		Composite timeoutComposite = new Composite(page, SWT.NONE);
		timeoutComposite.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING,
				true, false));
		timeoutComposite.setLayout(new GridLayout(2, false));
		Label timeoutLabel = new Label(timeoutComposite, SWT.NONE);
		timeoutLabel.setText("Authentication Timeout (ms): ");
		this.authenticationTimeoutText = new Text(timeoutComposite, SWT.BORDER);
		this.authenticationTimeoutText.setLayoutData(new GridData(SWT.FILL,
				SWT.BEGINNING, true, false));
		this.authenticationTimeoutText.setText(String
				.valueOf(initialAuthenticationTimeout));
		this.authenticationTimeoutText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				// Flag the name changed
				HttpSecuritySourceListingWizardPage.this.handleChange();
			}
		});

		// Indicate initial state
		this.handleChange();

		// Provide error if no HTTP Security loaders available
		if (this.httpSecuritySourceInstances.length == 0) {
			this.setErrorMessage("No HttpSecuritySource classes found");
		}

		// Specify the control
		this.setControl(page);
	}

	/**
	 * Handles a change.
	 */
	protected void handleChange() {

		// Reset to no selection
		this.selectedInstance = null;

		// Determine if HTTP Security source selected
		int selectionIndex = this.list.getSelectionIndex();
		if (selectionIndex >= 0) {
			// Selected HttpSecuritySource
			this.selectedInstance = this.httpSecuritySourceInstances[selectionIndex];
		} else {
			// No HttpSecuritySource selected
			this.setErrorMessage("Must select HttpSecuritySource");
			this.setPageComplete(false);
			return;
		}

		// Ensure have authentication timeout
		long authenticationTimeout;
		String authenticationTimeoutValue = this.authenticationTimeoutText
				.getText();
		if (EclipseUtil.isBlank(authenticationTimeoutValue)) {
			this.setErrorMessage("Must specify authentication timeout");
			this.setPageComplete(false);
			return;
		}
		try {
			authenticationTimeout = Long.parseLong(authenticationTimeoutValue);
		} catch (NumberFormatException ex) {
			this.setErrorMessage("Authentication timeout must be a number");
			this.setPageComplete(false);
			return;
		}

		// Provide authentication timeout
		this.selectedInstance.setAuthenticationTimeout(authenticationTimeout);

		// No issue
		this.setErrorMessage(null);
		this.setPageComplete(true);
	}

	/*
	 * ========================== CompilerIssues =========================
	 */

	@Override
	public void addIssue(LocationType locationType, String location,
			AssetType assetType, String assetName, String issueDescription) {
		// Provide as error message
		this.setErrorMessage(issueDescription);
	}

	@Override
	public void addIssue(LocationType locationType, String location,
			AssetType assetType, String assetName, String issueDescription,
			Throwable cause) {
		// Provide as error message
		this.setErrorMessage(issueDescription + " ("
				+ cause.getClass().getSimpleName() + ": " + cause.getMessage()
				+ ")");
	}

}