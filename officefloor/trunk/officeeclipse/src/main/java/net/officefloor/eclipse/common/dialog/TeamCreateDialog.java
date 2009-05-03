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
package net.officefloor.eclipse.common.dialog;

import java.util.List;
import java.util.Properties;

import net.officefloor.eclipse.classpath.ProjectClassLoader;
import net.officefloor.eclipse.common.dialog.input.Input;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.impl.BeanListInput;
import net.officefloor.eclipse.common.dialog.input.impl.SubTypeSelectionInput;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.source.TeamSource;
import net.officefloor.model.officefloor.OfficeFloorTeamModel;
import net.officefloor.model.officefloor.PropertyModel;

import org.eclipse.core.resources.IProject;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * {@link Dialog} to create a {@link OfficeFloorTeamModel}.
 * 
 * @author Daniel
 */
public class TeamCreateDialog extends Dialog {

	/**
	 * {@link IProject}.
	 */
	private final IProject project;

	/**
	 * {@link OfficeFloorTeamModel} to create.
	 */
	private OfficeFloorTeamModel team = null;

	/**
	 * {@link Text} to get the team name.
	 */
	private Text teamName;

	/**
	 * List to obtain the {@link TeamSource} class name.
	 */
	private InputHandler<String> teamSourceList;

	/**
	 * {@link Input} for the properties to create the {@link Team}.
	 */
	private BeanListInput<PropertyModel> propertiesInput;

	/**
	 * {@link InputHandler} for properties to create the {@link Team}.
	 */
	private InputHandler<List<PropertyModel>> propertiesHandler;

	/**
	 * Reports errors.
	 */
	private Label errorText;

	/**
	 * Initiate.
	 * 
	 * @param parentShell
	 *            Parent shell.
	 * @param project
	 *            {@link IProject}.
	 */
	public TeamCreateDialog(Shell parentShell, IProject project) {
		super(parentShell);
		this.project = project;
	}

	/**
	 * Creates the {@link OfficeFloorTeamModel}.
	 * 
	 * @return {@link OfficeFloorTeamModel}.
	 */
	public OfficeFloorTeamModel createTeam() throws Exception {

		// Block to open
		this.setBlockOnOpen(true);
		this.open();

		// Return the created team
		return this.team;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {

		// Create parent composite
		Composite composite = (Composite) super.createDialogArea(parent);

		// Enter team name
		new Label(composite, SWT.WRAP).setText("Name");
		this.teamName = new Text(composite, SWT.SINGLE | SWT.BORDER);

		// Enter the team source
		new Label(composite, SWT.WRAP).setText("Team Source");
		this.teamSourceList = new InputHandler<String>(composite,
				new SubTypeSelectionInput(this.project, TeamSource.class
						.getName()));

		// Enter the properties
		new Label(composite, SWT.WRAP).setText("Properties");
		this.propertiesInput = new BeanListInput<PropertyModel>(
				PropertyModel.class);
		this.propertiesInput.addProperty("name", 1);
		this.propertiesInput.addProperty("value", 2);
		this.propertiesHandler = new InputHandler<List<PropertyModel>>(
				composite, this.propertiesInput);

		// Error text
		this.errorText = new Label(composite, SWT.WRAP);
		this.errorText.setText("");
		this.errorText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL));
		this.errorText.setBackground(errorText.getDisplay().getSystemColor(
				SWT.COLOR_WIDGET_BACKGROUND));
		this.errorText.setForeground(ColorConstants.red);

		// Return the composite
		return composite;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {

		// Ensure team name provided
		String teamName = this.teamName.getText();
		if ((teamName == null) || (teamName.trim().length() == 0)) {
			this.errorText.setText("Enter team name");
			return;
		}

		// Ensure team source provided
		String teamSourceClassName = this.teamSourceList.getTrySafeValue();
		if ((teamSourceClassName == null)
				|| (teamSourceClassName.trim().length() == 0)) {
			this.errorText.setText("Select a team source");
			return;
		}

		// Obtain the properties to create the team
		PropertyModel[] propertyModels = this.propertiesHandler
				.getTrySafeValue().toArray(new PropertyModel[0]);
		Properties properties = new Properties();
		for (PropertyModel property : propertyModels) {
			properties.setProperty(property.getName(), property.getValue());
		}

		// Attempt to create the instance of the team
		TeamSource teamSource;
		try {
			ProjectClassLoader classLoader = ProjectClassLoader
					.create(this.project);
			Class<?> teamSourceClass = classLoader
					.loadClass(teamSourceClassName);
			Object instance = teamSourceClass.newInstance();
			if (!(instance instanceof TeamSource)) {
				throw new Exception(teamSourceClassName
						+ " must be an instance of "
						+ TeamSource.class.getName());
			}
			teamSource = (TeamSource) instance;
		} catch (Exception ex) {
			this.errorText.setText(ex.getMessage());
			return;
		}

		// Attempt to create the team
		try {
			// Attempt to create the team
			// TODO provide properties to team source
			Team team = teamSource.createTeam();

			// Ensure the team was created
			if (team == null) {
				throw new Exception(TeamSource.class.getSimpleName()
						+ " did not source a " + Team.class.getSimpleName());
			}
		} catch (Exception ex) {
			this.errorText.setText("Team failed creation: " + ex.getMessage());
			return;
		}

		// Team configuration valid (as at this point)
		this.team = new OfficeFloorTeamModel(teamName, teamSourceClassName,
				propertyModels, null, null);

		// Successful
		super.okPressed();
	}
}
