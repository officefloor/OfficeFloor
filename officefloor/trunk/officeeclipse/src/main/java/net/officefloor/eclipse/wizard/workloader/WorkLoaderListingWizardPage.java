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
package net.officefloor.eclipse.wizard.workloader;

import net.officefloor.work.WorkLoader;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

/**
 * {@link IWizardPage} providing the listing of {@link WorkLoader} instances.
 * 
 * @author Daniel
 */
public class WorkLoaderListingWizardPage extends WizardPage {

	/**
	 * Set of {@link WorkLoader} names.
	 */
	private final String[] workLoaderNames;

	/**
	 * List containing the {@link WorkLoader} names.
	 */
	private List list;

	/**
	 * Initiate.
	 * 
	 * @param pageName
	 */
	protected WorkLoaderListingWizardPage(String[] workLoaderNames) {
		super("WorkLoader listing");
		this.workLoaderNames = workLoaderNames;

		// Specify page details
		this.setTitle("Select a " + WorkLoader.class.getSimpleName());
	}

	/**
	 * Obtains the index of the selected {@link WorkLoader} or <code>-1</code>
	 * if one not selected.
	 * 
	 * @return Selected {@link WorkLoader} index.
	 */
	public int getSelectionIndex() {
		return this.list.getSelectionIndex();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	public void createControl(Composite parent) {

		// Fill the width
		Composite page = new Composite(parent, SWT.NULL);
		page.setLayout(new FillLayout(SWT.HORIZONTAL));

		// Add listing
		this.list = new List(page, SWT.SINGLE | SWT.BORDER);
		this.list.setItems(this.workLoaderNames);
		this.list.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Determine if selected
				boolean isSelected = (WorkLoaderListingWizardPage.this
						.getSelectionIndex() >= 0);

				// Page complete when work loader selected
				WorkLoaderListingWizardPage.this.setPageComplete(isSelected);
			}
		});

		// Initially page not complete (as must select work loader)
		this.setPageComplete(false);

		// Provide error if no work loaders available
		if (this.workLoaderNames.length == 0) {
			this.setErrorMessage("No WorkLoaders available");
		}

		// Specify the control
		this.setControl(page);
	}

}
