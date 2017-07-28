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
package net.officefloor.eclipse.wizard.officefunction;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import net.officefloor.compile.section.OfficeFunctionType;
import net.officefloor.compile.section.OfficeSectionType;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.eclipse.common.dialog.input.InputAdapter;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.impl.OfficeSectionInput;
import net.officefloor.frame.api.administration.Administration;

/**
 * {@link IWizardPage} to select the {@link OfficeFunctionType} from the
 * {@link OfficeSectionType}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFunctionSelectionWizardPage extends WizardPage {

	/**
	 * {@link OfficeSectionType}.
	 */
	private final OfficeSectionType sectionType;

	/**
	 * Radio {@link Button} indicating if pre {@link Administration}.
	 */
	private Button preAdministration;

	/**
	 * Selected {@link OfficeFunctionType}.
	 */
	private OfficeFunctionType selectedOfficeFunctionType = null;

	/**
	 * Initiate.
	 * 
	 * @param section
	 *            {@link OfficeSectionType}. May be <code>null</code> if failed
	 *            to load {@link OfficeSectionType}.
	 */
	OfficeFunctionSelectionWizardPage(OfficeSectionType sectionType) {
		super("OfficeSection load issues");
		this.sectionType = sectionType;

		// Specify page details
		this.setTitle("Select " + OfficeFunctionType.class.getSimpleName());
	}

	/**
	 * Obtains the selected {@link OfficeFunctionType}.
	 * 
	 * @return Selected {@link OfficeFunctionType} or <code>null</code> if no
	 *         {@link OfficeFunctionType} is selected.
	 */
	public OfficeFunctionType getSelectedOfficeFunctionType() {
		return this.selectedOfficeFunctionType;
	}

	/**
	 * Indicates if pre {@link Administration} rather than post
	 * {@link Administration}.
	 * 
	 * @return <code>true</code> if pre {@link Administration},
	 *         <code>false</code> if post {@link Administration}.
	 */
	public boolean isPreRatherThanPost() {
		// If not pre adminitration, then must be post administration
		return this.preAdministration.getSelection();
	}

	/*
	 * ==================== IDialogPage ======================================
	 */

	@Override
	public void createControl(Composite parent) {

		Composite page = new Composite(parent, SWT.NONE);
		page.setLayout(new GridLayout(1, true));

		// Determine if have office section type
		if (this.sectionType == null) {
			// No office section so provide error
			Label label = new Label(page, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
			label.setForeground(ColorConstants.red);
			label.setText("Failed to load the " + OfficeSection.class.getSimpleName());
			return;
		}

		// Add radio buttons to specify if pre or post administration
		Composite duty = new Composite(page, SWT.NONE);
		duty.setLayout(new RowLayout());
		new Label(duty, SWT.NONE).setText("Administer ");
		this.preAdministration = new Button(duty, SWT.RADIO);
		this.preAdministration.setText("pre");
		this.preAdministration.setSelection(true);
		Button postDuty = new Button(duty, SWT.RADIO);
		postDuty.setText("post");
		new Label(duty, SWT.NONE).setText(" function");

		// Have section so provide ability to select function type
		new InputHandler<OfficeFunctionType>(page, new OfficeSectionInput(this.sectionType, OfficeFunctionType.class),
				new InputAdapter() {
					@Override
					public void notifyValueChanged(Object value) {
						if (value instanceof OfficeFunctionType) {
							// Office function selected
							OfficeFunctionSelectionWizardPage.this.selectedOfficeFunctionType = (OfficeFunctionType) value;
							OfficeFunctionSelectionWizardPage.this.setErrorMessage(null);
							OfficeFunctionSelectionWizardPage.this.setPageComplete(true);

						} else {
							// Office function not selected
							OfficeFunctionSelectionWizardPage.this.selectedOfficeFunctionType = null;
							OfficeFunctionSelectionWizardPage.this
									.setErrorMessage("Select an " + OfficeFunctionType.class.getSimpleName());
							OfficeFunctionSelectionWizardPage.this.setPageComplete(false);
						}
					}
				});

		// Specify the control
		this.setControl(page);

		// Must select function before can complete page
		this.setPageComplete(false);
	}

}