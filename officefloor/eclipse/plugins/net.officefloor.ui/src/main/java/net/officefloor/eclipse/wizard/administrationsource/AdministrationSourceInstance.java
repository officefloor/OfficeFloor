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
package net.officefloor.eclipse.wizard.administrationsource;

import org.eclipse.core.resources.IProject;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.administration.AdministrationLoader;
import net.officefloor.compile.administration.AdministrationType;
import net.officefloor.compile.impl.issues.AbstractCompilerIssues;
import net.officefloor.compile.impl.issues.CompileException;
import net.officefloor.compile.impl.issues.DefaultCompilerIssue;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.administration.source.AdministrationSource;
import net.officefloor.compile.spi.administration.source.AdministrationSourceProperty;
import net.officefloor.compile.spi.office.OfficeAdministration;
import net.officefloor.eclipse.common.dialog.input.InputAdapter;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.impl.PropertyListInput;
import net.officefloor.eclipse.extension.administrationsource.AdministrationSourceExtension;
import net.officefloor.eclipse.extension.administrationsource.AdministrationSourceExtensionContext;
import net.officefloor.eclipse.util.EclipseUtil;

/**
 * {@link AdministrationSource} instance.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministrationSourceInstance extends AbstractCompilerIssues implements AdministrationSourceExtensionContext {

	/**
	 * Fully qualified class name of the {@link AdministrationSource}.
	 */
	private final String administrationSourceClassName;

	/**
	 * {@link AdministrationSourceExtension}. May be <code>null</code> if not
	 * obtained via extension point.
	 */
	private final AdministrationSourceExtension<?, ?, ?> administrationSourceExtension;

	/**
	 * {@link AdministrationLoader}.
	 */
	private final AdministrationLoader administrationLoader;

	/**
	 * {@link ClassLoader}.
	 */
	private final ClassLoader classLoader;

	/**
	 * {@link IProject}.
	 */
	private final IProject project;

	/**
	 * {@link AdministrationSourceInstanceContext}.
	 */
	private final AdministrationSourceInstanceContext context;

	/**
	 * {@link AdministrationSource} class.
	 */
	@SuppressWarnings("rawtypes")
	private Class administrationSourceClass;

	/**
	 * {@link PropertyList}.
	 */
	private PropertyList properties;

	/**
	 * {@link AdministrationType}.
	 */
	private AdministrationType<?, ?, ?> administrationType;

	/**
	 * Name of the {@link OfficeAdministration}.
	 */
	private String administrationName;

	/**
	 * Initiate.
	 * 
	 * @param administrationSourceClassName
	 *            Fully qualified class name of the {@link AdministrationSource}.
	 * @param administrationSourceExtension
	 *            {@link AdministrationSourceExtension}. May be <code>null</code>
	 *            .
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param project
	 *            {@link IProject}.
	 * @param context
	 *            {@link AdministrationSourceInstanceContext}.
	 */
	AdministrationSourceInstance(String administrationSourceClassName,
			AdministrationSourceExtension<?, ?, ?> administrationSourceExtension, ClassLoader classLoader,
			IProject project, AdministrationSourceInstanceContext context) {
		this.administrationSourceClassName = administrationSourceClassName;
		this.administrationSourceExtension = administrationSourceExtension;
		this.classLoader = classLoader;
		this.project = project;
		this.context = context;

		// Obtain the administrator loader
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(this.classLoader);
		compiler.setCompilerIssues(this);
		this.administrationLoader = compiler.getAdministrationLoader();
	}

	/**
	 * Specifies the name of the {@link OfficeAdministration}.
	 * 
	 * @param administrationName
	 *            Name of the {@link OfficeAdministration}.
	 */
	public void setAdministratorName(String administrationName) {
		this.administrationName = administrationName;

		// Notify properties changed as now have location
		this.notifyPropertiesChanged();
	}

	/**
	 * Attempts to load the {@link AdministrationType}.
	 */
	@SuppressWarnings("unchecked")
	public void loadAdministrationType() {

		// Ensure have name
		if (EclipseUtil.isBlank(this.administrationName)) {
			this.administrationType = null;
			this.setErrorMessage("Must specify administrator name");
			return; // must have name
		}

		// Ensure have administrator source class
		if (this.administrationSourceClass == null) {
			return; // page controls not yet loaded
		}

		// Ensure have properties
		if (this.properties == null) {
			return; // failed to load specification
		}

		// Attempt to load the administration type
		this.administrationType = this.administrationLoader.loadAdministrationType(this.administrationSourceClass,
				this.properties);
	}

	/**
	 * Obtains the label for the {@link AdministrationSource}.
	 * 
	 * @return Label for the {@link AdministrationSource}.
	 */
	public String getAdministrationSourceLabel() {
		if (this.administrationSourceExtension == null) {
			// No extension so use class name
			return this.administrationSourceClassName;
		} else {
			// Attempt to obtain from extension
			String name = this.administrationSourceExtension.getAdministrationSourceLabel();
			if (EclipseUtil.isBlank(name)) {
				// No name so use class name
				name = this.administrationSourceClassName;
			}
			return name;
		}
	}

	/**
	 * Obtains fully qualified class name of the {@link AdministrationSource}.
	 * 
	 * @return {@link AdministrationSource} class name.
	 */
	public String getAdministrationSourceClassName() {
		return this.administrationSourceClassName;
	}

	/**
	 * Obtains the name of the {@link OfficeAdministration}.
	 * 
	 * @return Name of the {@link OfficeAdministration}.
	 */
	public String getAdministratorName() {
		return this.administrationName;
	}

	/**
	 * Obtains the {@link PropertyList} to source the
	 * {@link OfficeAdministration} from the {@link AdministrationSource}.
	 * 
	 * @return Populated {@link PropertyList}.
	 */
	public PropertyList getPropertyList() {
		return this.properties;
	}

	/**
	 * Obtains the loaded {@link AdministrationType}.
	 * 
	 * @return Loaded {@link AdministrationType} or <code>null</code> if issue
	 *         loading.
	 */
	public AdministrationType<?, ?, ?> getAdministrationType() {
		return this.administrationType;
	}

	/**
	 * Creates the {@link Control} instances to populate the
	 * {@link AdministrationSourceProperty} instances.
	 * 
	 * @param page
	 *            {@link Composite} to add {@link Control} instances.
	 */
	@SuppressWarnings("unchecked")
	public void createControls(Composite page) {

		// Obtain the administrator source class
		if (this.administrationSourceExtension != null) {
			this.administrationSourceClass = this.administrationSourceExtension.getAdministrationSourceClass();
			if (this.administrationSourceClass == null) {
				page.setLayout(new GridLayout());
				Label label = new Label(page, SWT.NONE);
				label.setForeground(ColorConstants.red);
				label.setText("Extension did not provide class " + this.administrationSourceClassName);
				return;
			}
		} else {
			try {
				this.administrationSourceClass = (Class<? extends AdministrationSource<?, ?, ?>>) this.classLoader
						.loadClass(this.administrationSourceClassName);
			} catch (Throwable ex) {
				page.setLayout(new GridLayout());
				Label label = new Label(page, SWT.NONE);
				label.setForeground(ColorConstants.red);
				label.setText("Could not find class " + this.administrationSourceClassName + "\n\n"
						+ ex.getClass().getSimpleName() + ": " + ex.getMessage());
				return;
			}
		}

		// Obtain specification properties for administrator source
		this.properties = this.administrationLoader.loadSpecification(this.administrationSourceClass);
		if (this.properties == null) {
			page.setLayout(new GridLayout());
			Label label = new Label(page, SWT.NONE);
			label.setForeground(ColorConstants.red);
			label.setText("Could not obtain properties from specification for " + this.administrationSourceClassName);
			return;
		}

		// Determine if have extension
		if (this.administrationSourceExtension != null) {

			// Load page from extension
			try {
				this.administrationSourceExtension.createControl(page, this);
			} catch (Throwable ex) {
				// Failed to load page
				this.context.setErrorMessage(ex.getMessage() + " (" + ex.getClass().getSimpleName() + ")");
			}

		} else {
			// No an extension so provide properties table to fill out
			page.setLayout(new GridLayout());
			PropertyListInput propertyListInput = new PropertyListInput(this.properties);
			new InputHandler<PropertyList>(page, propertyListInput, new InputAdapter() {
				@Override
				public void notifyValueChanged(Object value) {
					AdministrationSourceInstance.this.notifyPropertiesChanged();
				}
			});
		}

		// Notify properties changed to set initial state
		this.notifyPropertiesChanged();
	}

	/*
	 * ============== AdministrationSourceExtensionContext ======================
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
		this.loadAdministrationType();

		// Flag whether the administrator type was loaded
		this.context.setAdministrationTypeLoaded(this.administrationType != null);
	}

	@Override
	public IProject getProject() {
		return this.project;
	}

	/*
	 * ===================== CompilerIssues ===============================
	 */

	@Override
	protected void handleDefaultIssue(DefaultCompilerIssue issue) {
		this.context.setErrorMessage(CompileException.toIssueString(issue));
	}

}