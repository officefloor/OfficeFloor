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

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.compile.team.TeamLoader;
import net.officefloor.compile.team.TeamType;
import net.officefloor.eclipse.common.dialog.input.InputAdapter;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.impl.PropertyListInput;
import net.officefloor.eclipse.extension.teamsource.TeamSourceExtension;
import net.officefloor.eclipse.extension.teamsource.TeamSourceExtensionContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.spi.team.source.TeamSource;
import net.officefloor.frame.spi.team.source.TeamSourceProperty;

import org.eclipse.core.resources.IProject;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * {@link TeamSource} instance.
 * 
 * @author Daniel Sagenschneider
 */
public class TeamSourceInstance implements TeamSourceExtensionContext,
		CompilerIssues {

	/**
	 * Fully qualified class name of the {@link TeamSource}.
	 */
	private final String teamSourceClassName;

	/**
	 * {@link TeamSourceExtension}. May be <code>null</code> if not obtained via
	 * extension point.
	 */
	private final TeamSourceExtension<?> teamSourceExtension;

	/**
	 * {@link TeamLoader}.
	 */
	private final TeamLoader teamLoader;

	/**
	 * {@link ClassLoader}.
	 */
	private final ClassLoader classLoader;

	/**
	 * {@link IProject}.
	 */
	private final IProject project;

	/**
	 * {@link TeamSourceInstanceContext}.
	 */
	private final TeamSourceInstanceContext context;

	/**
	 * {@link TeamSource} class.
	 */
	private Class<? extends TeamSource> teamSourceClass;

	/**
	 * {@link PropertyList}.
	 */
	private PropertyList properties;

	/**
	 * {@link TeamType}.
	 */
	private TeamType teamType;

	/**
	 * Name of the {@link OfficeTeam}.
	 */
	private String teamName;

	/**
	 * Initiate.
	 * 
	 * @param teamSourceClassName
	 *            Fully qualified class name of the {@link TeamSource}.
	 * @param teamSourceExtension
	 *            {@link TeamSourceExtension}. May be <code>null</code>.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param project
	 *            {@link IProject}.
	 * @param context
	 *            {@link TeamSourceInstanceContext}.
	 */
	TeamSourceInstance(String teamSourceClassName,
			TeamSourceExtension<?> teamSourceExtension,
			ClassLoader classLoader, IProject project,
			TeamSourceInstanceContext context) {
		this.teamSourceClassName = teamSourceClassName;
		this.teamSourceExtension = teamSourceExtension;
		this.classLoader = classLoader;
		this.project = project;
		this.context = context;

		// Obtain the team loader
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(this.classLoader);
		compiler.setCompilerIssues(this);
		this.teamLoader = compiler.getTeamLoader();
	}

	/**
	 * Specifies the location of the {@link OfficeTeam}.
	 * 
	 * @param teamName
	 *            Name of the {@link OfficeTeam}.
	 */
	public void setTeamName(String teamName) {
		this.teamName = teamName;

		// Notify properties changed as now have location
		this.notifyPropertiesChanged();
	}

	/**
	 * Attempts to load the {@link TeamType}.
	 */
	public void loadTeamType() {

		// Ensure have name
		if (EclipseUtil.isBlank(this.teamName)) {
			this.teamType = null;
			this.setErrorMessage("Must specify team name");
			return; // must have name
		}

		// Ensure have team source class
		if (this.teamSourceClass == null) {
			return; // page controls not yet loaded
		}

		// Attempt to load the team type
		this.teamType = this.teamLoader.loadTeamType(this.teamSourceClass,
				this.properties);
	}

	/**
	 * Obtains the label for the {@link TeamSource}.
	 * 
	 * @return Label for the {@link TeamSource}.
	 */
	public String getTeamSourceLabel() {
		if (this.teamSourceExtension == null) {
			// No extension so use class name
			return this.teamSourceClassName;
		} else {
			// Attempt to obtain from extension
			String name = this.teamSourceExtension.getTeamSourceLabel();
			if (EclipseUtil.isBlank(name)) {
				// No name so use class name
				name = this.teamSourceClassName;
			}
			return name;
		}
	}

	/**
	 * Obtains the fully qualified class name of the {@link TeamSource}.
	 * 
	 * @return {@link TeamSource} class name.
	 */
	public String getTeamSourceClassName() {
		return this.teamSourceClassName;
	}

	/**
	 * Obtains the name of the {@link OfficeTeam}.
	 * 
	 * @return Name of the {@link OfficeTeam}.
	 */
	public String getTeamName() {
		return this.teamName;
	}

	/**
	 * Obtains the {@link PropertyList} to source the {@link OfficeTeam} from
	 * the {@link TeamSource}.
	 * 
	 * @return Populated {@link PropertyList}.
	 */
	public PropertyList getPropertyList() {
		return this.properties;
	}

	/**
	 * Obtains the loaded {@link TeamType}.
	 * 
	 * @return Loaded {@link TeamType} or <code>null</code> if issue loading.
	 */
	public TeamType getTeamType() {
		return this.teamType;
	}

	/**
	 * Creates the {@link Control} instances to populate the
	 * {@link TeamSourceProperty} instances.
	 * 
	 * @param page
	 *            {@link Composite} to add {@link Control} instances.
	 */
	@SuppressWarnings("unchecked")
	public void createControls(Composite page) {

		// Obtain the team source class
		if (this.teamSourceExtension != null) {
			this.teamSourceClass = this.teamSourceExtension
					.getTeamSourceClass();
			if (this.teamSourceClass == null) {
				page.setLayout(new GridLayout());
				Label label = new Label(page, SWT.NONE);
				label.setForeground(ColorConstants.red);
				label.setText("Extension did not provide class "
						+ this.teamSourceClassName);
				return;
			}
		} else {
			try {
				this.teamSourceClass = (Class<? extends TeamSource>) this.classLoader
						.loadClass(this.teamSourceClassName);
			} catch (Throwable ex) {
				page.setLayout(new GridLayout());
				Label label = new Label(page, SWT.NONE);
				label.setForeground(ColorConstants.red);
				label.setText("Could not find class "
						+ this.teamSourceClassName + "\n\n"
						+ ex.getClass().getSimpleName() + ": "
						+ ex.getMessage());
				return;
			}
		}

		// Obtain specification properties for team source
		this.properties = this.teamLoader.loadSpecification(teamSourceClass);

		// Determine if have extension
		if (this.teamSourceExtension != null) {

			// Load page from extension
			try {
				this.teamSourceExtension.createControl(page, this);
			} catch (Throwable ex) {
				// Failed to load page
				this.context.setErrorMessage(ex.getMessage() + " ("
						+ ex.getClass().getSimpleName() + ")");
			}

		} else {
			// No an extension so provide properties table to fill out
			page.setLayout(new GridLayout());
			PropertyListInput propertyListInput = new PropertyListInput(
					this.properties);
			new InputHandler<PropertyList>(page, propertyListInput,
					new InputAdapter() {
						@Override
						public void notifyValueChanged(Object value) {
							TeamSourceInstance.this.notifyPropertiesChanged();
						}
					});
		}

		// Notify properties changed to set initial state
		this.notifyPropertiesChanged();
	}

	/*
	 * ================== TeamSourceExtensionContext ======================
	 */

	@Override
	public void setTitle(String title) {
		this.context.setTitle(title);
	}

	@Override
	public void setErrorMessage(String message) {
		this.context.setErrorMessage(message);
	}

	@Override
	public void notifyPropertiesChanged() {

		// Clear the error message
		this.context.setErrorMessage(null);

		// Attempt to load the team type.
		// Issues notified back via the team loader.
		this.loadTeamType();

		// Flag whether the team type was loaded
		this.context.setTeamTypeLoaded(this.teamType != null);
	}

	@Override
	public IProject getProject() {
		return this.project;
	}

	/*
	 * ===================== CompilerIssues ===============================
	 */

	@Override
	public void addIssue(LocationType locationType, String location,
			AssetType assetType, String assetName, String issueDescription) {
		// Provide as error message
		this.context.setErrorMessage(issueDescription);
	}

	@Override
	public void addIssue(LocationType locationType, String location,
			AssetType assetType, String assetName, String issueDescription,
			Throwable cause) {
		// Provide as error message
		this.context.setErrorMessage(issueDescription + " ("
				+ cause.getClass().getSimpleName() + ": " + cause.getMessage()
				+ ")");
	}

}