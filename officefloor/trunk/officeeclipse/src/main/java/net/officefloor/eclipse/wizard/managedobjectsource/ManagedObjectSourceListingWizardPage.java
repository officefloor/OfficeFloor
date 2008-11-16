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
package net.officefloor.eclipse.wizard.managedobjectsource;

import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

/**
 * {@link IWizardPage} providing the listing of {@link ManagedObjectSource}
 * instances.
 * 
 * @author Daniel
 */
public class ManagedObjectSourceListingWizardPage extends WizardPage {

	/**
	 * Set of {@link ManagedObjectSource} names.
	 */
	private final String[] managedObjectSourceNames;

	/**
	 * List containing the {@link ManagedObjectSource} names.
	 */
	private List list;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectSourceNames
	 *            Names of the {@link ManagedObjectSource} instances.
	 */
	protected ManagedObjectSourceListingWizardPage(
			String[] managedObjectSourceNames) {
		super("ManagedObjectSource listing");
		this.managedObjectSourceNames = managedObjectSourceNames;

		// Specify page details
		this.setTitle("Select a " + ManagedObjectSource.class.getSimpleName());
	}

	/**
	 * Obtains the index of the selected {@link ManagedObjectSource} or
	 * <code>-1</code> if one not selected.
	 * 
	 * @return Selected {@link ManagedObjectSource} index.
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
		this.list.setItems(this.managedObjectSourceNames);
		this.list.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Determine if selected
				boolean isSelected = (ManagedObjectSourceListingWizardPage.this
						.getSelectionIndex() >= 0);

				// Page complete when managed object source selected
				ManagedObjectSourceListingWizardPage.this
						.setPageComplete(isSelected);
			}
		});

		// Initially page not complete (as must select managed object source)
		this.setPageComplete(false);

		// Provide error if no managed object sources available
		if (this.managedObjectSourceNames.length == 0) {
			this.setErrorMessage("No ManagedObjectSources available");
		}

		// Specify the control
		this.setControl(page);
	}

}
