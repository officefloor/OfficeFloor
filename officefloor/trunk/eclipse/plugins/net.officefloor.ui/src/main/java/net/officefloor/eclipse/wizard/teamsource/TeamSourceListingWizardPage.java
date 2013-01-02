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
package net.officefloor.eclipse.wizard.teamsource;

import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.source.TeamSource;

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
 * {@link IWizardPage} providing the listing of {@link TeamSourceInstance}.
 * 
 * @author Daniel Sagenschneider
 */
public class TeamSourceListingWizardPage extends WizardPage {

	/**
	 * {@link TeamSourceInstance} listing.
	 */
	private final TeamSourceInstance[] teamSourceInstances;

	/**
	 * Listing of {@link TeamSource} labels in order of
	 * {@link TeamSourceInstance} listing.
	 */
	private final String[] teamSourceLabels;

	/**
	 * {@link Text} of the {@link Team} name.
	 */
	private Text teamName;

	/**
	 * List containing the {@link TeamSource} labels.
	 */
	private List list;

	/**
	 * Initiate.
	 * 
	 * @param teamSourceInstances
	 *            Listing of {@link TeamSourceInstance}.
	 */
	TeamSourceListingWizardPage(TeamSourceInstance[] teamSourceInstances) {
		super("TeamSource listing");
		this.teamSourceInstances = teamSourceInstances;

		// Create the listing of labels
		this.teamSourceLabels = new String[this.teamSourceInstances.length];
		for (int i = 0; i < this.teamSourceLabels.length; i++) {
			this.teamSourceLabels[i] = this.teamSourceInstances[i]
					.getTeamSourceLabel();
		}

		// Specify page details
		this.setTitle("Select a " + TeamSource.class.getSimpleName());
	}

	/**
	 * Obtains the selected {@link TeamSourceInstance}.
	 * 
	 * @return Selected {@link TeamSourceInstance} or <code>null</code> if not
	 *         selected.
	 */
	public TeamSourceInstance getSelectedTeamSourceInstance() {
		int selectedIndex = this.list.getSelectionIndex();
		if (selectedIndex < 0) {
			// No selected team source instance
			return null;
		} else {
			// Return the selected team source instance
			return this.teamSourceInstances[selectedIndex];
		}
	}

	/*
	 * ==================== IDialogPage ======================================
	 */

	@Override
	public void createControl(Composite parent) {

		Composite page = new Composite(parent, SWT.NONE);
		page.setLayout(new GridLayout(1, true));

		// Add means to specify team name
		Composite nameComposite = new Composite(page, SWT.NONE);
		nameComposite.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true,
				false));
		nameComposite.setLayout(new GridLayout(2, false));
		Label nameLabel = new Label(nameComposite, SWT.NONE);
		nameLabel.setText("Team name: ");
		this.teamName = new Text(nameComposite, SWT.BORDER);
		this.teamName.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true,
				false));
		this.teamName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				// Flag the name changed
				TeamSourceListingWizardPage.this.handleChange();
			}
		});

		// Add listing of team sources
		this.list = new List(page, SWT.SINGLE | SWT.BORDER);
		this.list.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true,
				false));
		this.list.setItems(this.teamSourceLabels);
		this.list.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TeamSourceListingWizardPage.this.handleChange();
			}
		});

		// Initially page not complete (as must select team source)
		this.setPageComplete(false);

		// Provide error if no team loaders available
		if (this.teamSourceInstances.length == 0) {
			this.setErrorMessage("No TeamSource classes found");
		}

		// Specify the control
		this.setControl(page);
	}

	/**
	 * Handles a change.
	 */
	protected void handleChange() {

		// Ensure have team name
		String name = this.teamName.getText();
		if (EclipseUtil.isBlank(name)) {
			this.setErrorMessage("Must specify name of team");
			this.setPageComplete(false);
			return;
		}

		// Determine if team source selected
		int selectionIndex = this.list.getSelectionIndex();
		if (selectionIndex < 0) {
			this.setErrorMessage("Must select TeamSource");
			this.setPageComplete(false);
			return;
		}

		// Specify name for team source and is complete
		this.teamSourceInstances[selectionIndex].setTeamName(name);
		this.setErrorMessage(null);
		this.setPageComplete(true);
	}

}