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
package net.officefloor.eclipse.wizard.sectionsource;

import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.eclipse.classpath.ClasspathUtil;
import net.officefloor.eclipse.common.dialog.input.ClasspathFilter;
import net.officefloor.eclipse.common.dialog.input.InputFilter;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.InputListener;
import net.officefloor.eclipse.common.dialog.input.impl.ClasspathSelectionInput;
import net.officefloor.eclipse.util.EclipseUtil;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

/**
 * {@link IWizardPage} providing the listing of {@link SectionSourceInstance}.
 * 
 * @author Daniel
 */
public class SectionSourceListingWizardPage extends WizardPage {

	/**
	 * {@link SectionSourceInstance} listing.
	 */
	private final SectionSourceInstance[] sectionSourceInstances;

	/**
	 * {@link IProject}.
	 */
	private final IProject project;

	/**
	 * Listing of {@link SectionSource} labels in order of
	 * {@link SectionSourceInstance} listing.
	 */
	private final String[] sectionSourceLabels;

	/**
	 * List containing the {@link SectionSource} labels.
	 */
	private List list;

	/**
	 * Location of the {@link OfficeSection}.
	 */
	private String sectionLocation;

	/**
	 * Initiate.
	 * 
	 * @param sectionSourceInstances
	 *            Listing of {@link SectionSourceInstance}.
	 * @param project
	 *            {@link IProject}.
	 */
	SectionSourceListingWizardPage(
			SectionSourceInstance[] sectionSourceInstances, IProject project) {
		super("SectionSource listing");
		this.sectionSourceInstances = sectionSourceInstances;
		this.project = project;

		// Create the listing of labels
		this.sectionSourceLabels = new String[this.sectionSourceInstances.length];
		for (int i = 0; i < this.sectionSourceLabels.length; i++) {
			this.sectionSourceLabels[i] = this.sectionSourceInstances[i]
					.getSectionSourceLabel();
		}

		// Specify page details
		this.setTitle("Select a " + SectionSource.class.getSimpleName());
	}

	/**
	 * Obtains the selected {@link SectionSourceInstance}.
	 * 
	 * @return Selected {@link SectionSourceInstance} or <code>null</code> if
	 *         not selected.
	 */
	public SectionSourceInstance getSelectedSectionSourceInstance() {
		int selectedIndex = this.list.getSelectionIndex();
		if (selectedIndex < 0) {
			// No selected section source instance
			return null;
		} else {
			// Return the selected section source instance
			return this.sectionSourceInstances[selectedIndex];
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

		// Add listing of section sources
		this.list = new List(page, SWT.SINGLE | SWT.BORDER);
		this.list.setItems(this.sectionSourceLabels);
		this.list.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SectionSourceListingWizardPage.this.handleChange();
			}
		});

		// Provide means to specify section location
		Composite location = new Composite(page, SWT.NONE);
		location.setLayout(new GridLayout(1, true));
		ClasspathFilter filter = new ClasspathFilter();
		filter.addFileFilter(new InputFilter<IFile>() {
			@Override
			public boolean isFilter(IFile item) {
				return false; // include all files
			}
		});
		new InputHandler<String>(location, new ClasspathSelectionInput(project,
				filter), new InputListener() {
			@Override
			public void notifyValueChanged(Object value) {

				// Handle if java element
				if (value instanceof IJavaElement) {
					value = ((IJavaElement) value).getResource();
				}

				// Obtain the location
				if (value instanceof IFile) {
					IFile file = (IFile) value;
					SectionSourceListingWizardPage.this.sectionLocation = ClasspathUtil
							.getClassPathLocation(file.getFullPath());
				} else {
					// No file selected
					SectionSourceListingWizardPage.this.sectionLocation = null;
				}
				SectionSourceListingWizardPage.this.handleChange();
			}

			@Override
			public void notifyValueInvalid(String message) {
				SectionSourceListingWizardPage.this.setErrorMessage(message);
			}
		});

		// Initially page not complete (as must select section source)
		this.setPageComplete(false);

		// Provide error if no section loaders available
		if (this.sectionSourceInstances.length == 0) {
			this.setErrorMessage("No SectionSource classes found");
		}

		// Specify the control
		this.setControl(page);
	}

	/**
	 * Handles a change.
	 */
	protected void handleChange() {

		// Determine if section source selected
		int selectionIndex = this.list.getSelectionIndex();
		if (selectionIndex < 0) {
			this.setErrorMessage("Must select SectionSource");
			this.setPageComplete(false);
			return;
		}

		// Ensure have section location
		if (EclipseUtil.isBlank(this.sectionLocation)) {
			this.setErrorMessage("Must specify location of section");
			this.setPageComplete(false);
			return;
		}

		// Specify location for section source and is complete
		this.sectionSourceInstances[selectionIndex]
				.setSectionLocation(this.sectionLocation);
		this.setErrorMessage(null);
		this.setPageComplete(true);
	}

}