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

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.administrator.AdministratorLoader;
import net.officefloor.compile.administrator.AdministratorType;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeAdministrator;
import net.officefloor.eclipse.common.dialog.input.InputAdapter;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.impl.PropertyListInput;
import net.officefloor.eclipse.extension.administratorsource.AdministratorSourceExtension;
import net.officefloor.eclipse.extension.administratorsource.AdministratorSourceExtensionContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.spi.administration.source.AdministratorSource;

import org.eclipse.core.resources.IProject;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * {@link AdministratorSource} instance.
 * 
 * @author Daniel
 */
public class AdministratorSourceInstance implements
		AdministratorSourceExtensionContext, CompilerIssues {

	/**
	 * Fully qualified class name of the {@link AdministratorSource}.
	 */
	private final String administratorSourceClassName;

	/**
	 * {@link AdministratorSourceExtension}. May be <code>null</code> if not
	 * obtained via extension point.
	 */
	private final AdministratorSourceExtension<?, ?, ?> administratorSourceExtension;

	/**
	 * {@link AdministratorLoader}.
	 */
	private final AdministratorLoader administratorLoader;

	/**
	 * {@link ClassLoader}.
	 */
	private final ClassLoader classLoader;

	/**
	 * {@link IProject}.
	 */
	private final IProject project;

	/**
	 * {@link AdministratorSourceInstanceContext}.
	 */
	private final AdministratorSourceInstanceContext context;

	/**
	 * {@link AdministratorSource} class.
	 */
	@SuppressWarnings("unchecked")
	private Class<? extends AdministratorSource> administratorSourceClass;

	/**
	 * {@link PropertyList}.
	 */
	private PropertyList properties;

	/**
	 * {@link AdministratorType}.
	 */
	private AdministratorType<?, ?> administratorType;

	/**
	 * Name of the {@link OfficeAdministrator}.
	 */
	private String administratorName;

	/**
	 * Initiate.
	 * 
	 * @param administratorSourceClassName
	 *            Fully qualified class name of the {@link AdministratorSource}.
	 * @param administratorSourceExtension
	 *            {@link AdministratorSourceExtension}. May be <code>null</code>
	 *            .
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param project
	 *            {@link IProject}.
	 * @param context
	 *            {@link AdministratorSourceInstanceContext}.
	 */
	AdministratorSourceInstance(String administratorSourceClassName,
			AdministratorSourceExtension<?, ?, ?> administratorSourceExtension,
			ClassLoader classLoader, IProject project,
			AdministratorSourceInstanceContext context) {
		this.administratorSourceClassName = administratorSourceClassName;
		this.administratorSourceExtension = administratorSourceExtension;
		this.classLoader = classLoader;
		this.project = project;
		this.context = context;

		// Obtain the administrator loader
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler();
		compiler.setClassLoader(this.classLoader);
		compiler.setCompilerIssues(this);
		this.administratorLoader = compiler.getAdministratorLoader();
	}

	/**
	 * Specifies the location of the {@link OfficeAdministrator}.
	 * 
	 * @param administratorName
	 *            Name of the {@link OfficeAdministrator}.
	 */
	public void setAdministratorName(String administratorName) {
		this.administratorName = administratorName;

		// Notify properties changed as now have location
		this.notifyPropertiesChanged();
	}

	/**
	 * Attempts to load the {@link AdministratorType}.
	 */
	public void loadAdministratorType() {

		// Ensure have name
		if (EclipseUtil.isBlank(this.administratorName)) {
			this.administratorType = null;
			this.setErrorMessage("Must specify administrator name");
			return; // must have name
		}

		// Ensure have administrator source class
		if (this.administratorSourceClass == null) {
			return; // page controls not yet loaded
		}

		// Ensure have properties
		if (this.properties == null) {
			return; // failed to load specification
		}

		// Attempt to load the administrator type
		this.administratorType = this.administratorLoader.loadAdministrator(
				this.administratorSourceClass, this.properties);
	}

	/**
	 * Obtains the label for the {@link AdministratorSource}.
	 * 
	 * @return Label for the {@link AdministratorSource}.
	 */
	public String getAdministratorSourceLabel() {
		if (this.administratorSourceExtension == null) {
			// No extension so use class name
			return this.administratorSourceClassName;
		} else {
			// Attempt to obtain from extension
			String name = this.administratorSourceExtension
					.getAdministratorSourceLabel();
			if (EclipseUtil.isBlank(name)) {
				// No name so use class name
				name = this.administratorSourceClassName;
			}
			return name;
		}
	}

	/**
	 * Obtains the fully qualified class name of the {@link AdministratorSource}
	 * .
	 * 
	 * @return {@link AdministratorSource} class name.
	 */
	public String getAdministratorSourceClassName() {
		return this.administratorSourceClassName;
	}

	/**
	 * Obtains the name of the {@link OfficeAdministrator}.
	 * 
	 * @return Name of the {@link OfficeAdministrator}.
	 */
	public String getAdministratorName() {
		return this.administratorName;
	}

	/**
	 * Obtains the {@link PropertyList} to source the
	 * {@link OfficeAdministrator} from the {@link AdministratorSource}.
	 * 
	 * @return Populated {@link PropertyList}.
	 */
	public PropertyList getPropertyList() {
		return this.properties;
	}

	/**
	 * Obtains the loaded {@link AdministratorType}.
	 * 
	 * @return Loaded {@link AdministratorType} or <code>null</code> if issue
	 *         loading.
	 */
	public AdministratorType<?, ?> getAdministratorType() {
		return this.administratorType;
	}

	/**
	 * Creates the {@link Control} instances to populate the
	 * {@link AdministratorLoaderProperty} instances.
	 * 
	 * @param page
	 *            {@link Composite} to add {@link Control} instances.
	 * @param context
	 *            {@link AdministratorSourceInstanceContext}.
	 */
	@SuppressWarnings("unchecked")
	public void createControls(Composite page) {

		// Obtain the administrator source class
		if (this.administratorSourceExtension != null) {
			this.administratorSourceClass = this.administratorSourceExtension
					.getAdministratorSourceClass();
			if (this.administratorSourceClass == null) {
				page.setLayout(new GridLayout());
				Label label = new Label(page, SWT.NONE);
				label.setForeground(ColorConstants.red);
				label.setText("Extension did not provide class "
						+ this.administratorSourceClassName);
				return;
			}
		} else {
			try {
				this.administratorSourceClass = (Class<? extends AdministratorSource<?, ?>>) this.classLoader
						.loadClass(this.administratorSourceClassName);
			} catch (Throwable ex) {
				page.setLayout(new GridLayout());
				Label label = new Label(page, SWT.NONE);
				label.setForeground(ColorConstants.red);
				label.setText("Could not find class "
						+ this.administratorSourceClassName + "\n\n"
						+ ex.getClass().getSimpleName() + ": "
						+ ex.getMessage());
				return;
			}
		}

		// Obtain specification properties for administrator source
		this.properties = this.administratorLoader
				.loadSpecification(this.administratorSourceClass);
		if (this.properties == null) {
			page.setLayout(new GridLayout());
			Label label = new Label(page, SWT.NONE);
			label.setForeground(ColorConstants.red);
			label.setText("Could not obtain properties from specification for "
					+ this.administratorSourceClassName);
			return;
		}

		// Determine if have extension
		if (this.administratorSourceExtension != null) {

			// Load page from extension
			try {
				this.administratorSourceExtension.createControl(page, this);
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
							AdministratorSourceInstance.this
									.notifyPropertiesChanged();
						}
					});
		}

		// Notify properties changed to set initial state
		this.notifyPropertiesChanged();
	}

	/*
	 * ============== AdministratorSourceExtensionContext ======================
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

		// Attempt to load the administrator type.
		// Issues notified back via the administrator loader.
		this.loadAdministratorType();

		// Flag whether the administrator type was loaded
		this.context.setAdministratorTypeLoaded(this.administratorType != null);
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