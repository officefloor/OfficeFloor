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
package net.officefloor.eclipse.wizard.administratorsource;

import org.eclipse.core.resources.IProject;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.administrator.AdministratorLoader;
import net.officefloor.compile.administrator.AdministratorType;
import net.officefloor.compile.impl.issues.AbstractCompilerIssues;
import net.officefloor.compile.impl.issues.CompileException;
import net.officefloor.compile.impl.issues.DefaultCompilerIssue;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeAdministrator;
import net.officefloor.eclipse.common.dialog.input.InputAdapter;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.impl.PropertyListInput;
import net.officefloor.eclipse.extension.administratorsource.AdministratorSourceExtension;
import net.officefloor.eclipse.extension.administratorsource.AdministratorSourceExtensionContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.frame.internal.structure.AdministratorScope;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.administration.source.AdministratorSourceProperty;

/**
 * {@link AdministratorSource} instance.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministratorSourceInstance extends AbstractCompilerIssues implements AdministratorSourceExtensionContext {

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
	@SuppressWarnings("rawtypes")
	private Class administratorSourceClass;

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
	 * {@link AdministratorScope} for the {@link OfficeAdministrator}.
	 */
	private AdministratorScope administratorScope;

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
			AdministratorSourceExtension<?, ?, ?> administratorSourceExtension, ClassLoader classLoader,
			IProject project, AdministratorSourceInstanceContext context) {
		this.administratorSourceClassName = administratorSourceClassName;
		this.administratorSourceExtension = administratorSourceExtension;
		this.classLoader = classLoader;
		this.project = project;
		this.context = context;

		// Obtain the administrator loader
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(this.classLoader);
		compiler.setCompilerIssues(this);
		this.administratorLoader = compiler.getAdministratorLoader();
	}

	/**
	 * Specifies the name and {@link AdministratorScope} of the
	 * {@link OfficeAdministrator}.
	 * 
	 * @param administratorName
	 *            Name of the {@link OfficeAdministrator}.
	 * @param administratorScope
	 *            {@link AdministratorScope}.
	 */
	public void setAdministratorNameAndScope(String administratorName, AdministratorScope administratorScope) {
		this.administratorName = administratorName;
		this.administratorScope = administratorScope;

		// Notify properties changed as now have location
		this.notifyPropertiesChanged();
	}

	/**
	 * Attempts to load the {@link AdministratorType}.
	 */
	@SuppressWarnings("unchecked")
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
		this.administratorType = this.administratorLoader.loadAdministratorType(this.administratorSourceClass,
				this.properties);
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
			String name = this.administratorSourceExtension.getAdministratorSourceLabel();
			if (EclipseUtil.isBlank(name)) {
				// No name so use class name
				name = this.administratorSourceClassName;
			}
			return name;
		}
	}

	/**
	 * Obtains fully qualified class name of the {@link AdministratorSource}.
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
	 * Obtains the {@link AdministratorScope} for the
	 * {@link OfficeAdministrator}.
	 * 
	 * @return {@link AdministratorScope} for the {@link OfficeAdministrator}.
	 */
	public AdministratorScope getAdministratorScope() {
		return this.administratorScope;
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
	 * {@link AdministratorSourceProperty} instances.
	 * 
	 * @param page
	 *            {@link Composite} to add {@link Control} instances.
	 */
	@SuppressWarnings("unchecked")
	public void createControls(Composite page) {

		// Obtain the administrator source class
		if (this.administratorSourceExtension != null) {
			this.administratorSourceClass = this.administratorSourceExtension.getAdministratorSourceClass();
			if (this.administratorSourceClass == null) {
				page.setLayout(new GridLayout());
				Label label = new Label(page, SWT.NONE);
				label.setForeground(ColorConstants.red);
				label.setText("Extension did not provide class " + this.administratorSourceClassName);
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
				label.setText("Could not find class " + this.administratorSourceClassName + "\n\n"
						+ ex.getClass().getSimpleName() + ": " + ex.getMessage());
				return;
			}
		}

		// Obtain specification properties for administrator source
		this.properties = this.administratorLoader.loadSpecification(this.administratorSourceClass);
		if (this.properties == null) {
			page.setLayout(new GridLayout());
			Label label = new Label(page, SWT.NONE);
			label.setForeground(ColorConstants.red);
			label.setText("Could not obtain properties from specification for " + this.administratorSourceClassName);
			return;
		}

		// Determine if have extension
		if (this.administratorSourceExtension != null) {

			// Load page from extension
			try {
				this.administratorSourceExtension.createControl(page, this);
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
					AdministratorSourceInstance.this.notifyPropertiesChanged();
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
	protected void handleDefaultIssue(DefaultCompilerIssue issue) {
		this.context.setErrorMessage(CompileException.toIssueString(issue));
	}

}