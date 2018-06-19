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
package net.officefloor.eclipse.wizard.officesource;

import org.eclipse.core.resources.IProject;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.issues.AbstractCompilerIssues;
import net.officefloor.compile.impl.issues.CompileException;
import net.officefloor.compile.impl.issues.DefaultCompilerIssue;
import net.officefloor.compile.office.OfficeLoader;
import net.officefloor.compile.office.OfficeType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.office.source.OfficeSourceProperty;
import net.officefloor.eclipse.common.dialog.input.InputAdapter;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.impl.PropertyListInput;
import net.officefloor.eclipse.extension.officesource.OfficeSourceExtension;
import net.officefloor.eclipse.extension.officesource.OfficeSourceExtensionContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.frame.api.manage.Office;

/**
 * {@link OfficeSource} instance.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeSourceInstance extends AbstractCompilerIssues implements OfficeSourceExtensionContext {

	/**
	 * Fully qualified class name of the {@link OfficeSource}.
	 */
	private final String officeSourceClassName;

	/**
	 * {@link OfficeSourceExtension}. May be <code>null</code> if not obtained
	 * via extension point.
	 */
	private final OfficeSourceExtension<?> officeSourceExtension;

	/**
	 * {@link OfficeLoader}.
	 */
	private final OfficeLoader officeLoader;

	/**
	 * {@link ClassLoader}.
	 */
	private final ClassLoader classLoader;

	/**
	 * {@link IProject}.
	 */
	private final IProject project;

	/**
	 * {@link OfficeSourceInstanceContext}.
	 */
	private final OfficeSourceInstanceContext context;

	/**
	 * {@link OfficeSource} class.
	 */
	private Class<? extends OfficeSource> officeSourceClass;

	/**
	 * {@link PropertyList}.
	 */
	private PropertyList properties;

	/**
	 * {@link OfficeType}.
	 */
	private OfficeType officeType;

	/**
	 * Name of the {@link Office}.
	 */
	private String officeName;

	/**
	 * Location of the {@link Office}.
	 */
	private String officeLocation;

	/**
	 * Initiate.
	 * 
	 * @param officeSourceClassName
	 *            Fully qualified class name of the {@link OfficeSource}.
	 * @param officeSourceExtension
	 *            {@link OfficeSourceExtension}. May be <code>null</code>.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param project
	 *            {@link IProject}.
	 * @param context
	 *            {@link OfficeSourceInstanceContext}.
	 */
	OfficeSourceInstance(String officeSourceClassName, OfficeSourceExtension<?> officeSourceExtension,
			ClassLoader classLoader, IProject project, OfficeSourceInstanceContext context) {
		this.officeSourceClassName = officeSourceClassName;
		this.officeSourceExtension = officeSourceExtension;
		this.classLoader = classLoader;
		this.project = project;
		this.context = context;

		// Obtain the office loader
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(this.classLoader);
		compiler.setCompilerIssues(this);
		this.officeLoader = compiler.getOfficeLoader();
	}

	/**
	 * Specifies the location of the {@link Office}.
	 * 
	 * @param officeName
	 *            Name of the {@link Office}.
	 * @param officeLocation
	 *            Location of the {@link Office}.
	 */
	public void setOfficeNameAndLocation(String officeName, String officeLocation) {
		this.officeName = officeName;
		this.officeLocation = officeLocation;

		// Notify properties changed as now have location
		this.notifyPropertiesChanged();
	}

	/**
	 * Attempts to load the {@link OfficeType}.
	 */
	public void loadOfficeType() {

		// Ensure have name
		if (EclipseUtil.isBlank(this.officeName)) {
			this.officeType = null;
			this.setErrorMessage("Must specify office name");
			return; // must have name
		}

		// Ensure have location
		if (EclipseUtil.isBlank(this.officeLocation)) {
			this.officeType = null;
			this.setErrorMessage("Must specify office location");
			return; // must have location
		}

		// Ensure have office source class
		if (this.officeSourceClass == null) {
			return; // page controls not yet loaded
		}

		// Attempt to load the office type
		this.officeType = this.officeLoader.loadOfficeType(this.officeSourceClass, this.officeLocation,
				this.properties);
	}

	/**
	 * Obtains the label for the {@link OfficeSource}.
	 * 
	 * @return Label for the {@link OfficeSource}.
	 */
	public String getOfficeSourceLabel() {
		if (this.officeSourceExtension == null) {
			// No extension so use class name
			return this.officeSourceClassName;
		} else {
			// Attempt to obtain from extension
			String name = this.officeSourceExtension.getOfficeSourceLabel();
			if (EclipseUtil.isBlank(name)) {
				// No name so use class name
				name = this.officeSourceClassName;
			}
			return name;
		}
	}

	/**
	 * Obtains the fully qualified class name of the {@link OfficeSource}.
	 * 
	 * @return {@link OfficeSource} class name.
	 */
	public String getOfficeSourceClassName() {
		return this.officeSourceClassName;
	}

	/**
	 * Obtains the name of the {@link Office}.
	 * 
	 * @return Name of the {@link Office}.
	 */
	public String getOfficeName() {
		return this.officeName;
	}

	/**
	 * Obtains the location of the {@link Office}.
	 * 
	 * @return Location of the {@link Office}.
	 */
	public String getOfficeLocation() {
		return this.officeLocation;
	}

	/**
	 * Obtains the {@link PropertyList} to source the {@link Office} from the
	 * {@link OfficeSource}.
	 * 
	 * @return Populated {@link PropertyList}.
	 */
	public PropertyList getPropertyList() {
		return this.properties;
	}

	/**
	 * Obtains the loaded {@link OfficeType}.
	 * 
	 * @return Loaded {@link OfficeType} or <code>null</code> if issue loading.
	 */
	public OfficeType getOfficeType() {
		return this.officeType;
	}

	/**
	 * Creates the {@link Control} instances to populate the
	 * {@link OfficeSourceProperty} instances.
	 * 
	 * @param page
	 *            {@link Composite} to add {@link Control} instances.
	 */
	@SuppressWarnings("unchecked")
	public void createControls(Composite page) {

		// Obtain the office source class
		if (this.officeSourceExtension != null) {
			this.officeSourceClass = this.officeSourceExtension.getOfficeSourceClass();
			if (this.officeSourceClass == null) {
				page.setLayout(new GridLayout());
				Label label = new Label(page, SWT.NONE);
				label.setForeground(ColorConstants.red);
				label.setText("Extension did not provide class " + this.officeSourceClassName);
				return;
			}
		} else {
			try {
				this.officeSourceClass = (Class<? extends OfficeSource>) this.classLoader
						.loadClass(this.officeSourceClassName);
			} catch (Throwable ex) {
				page.setLayout(new GridLayout());
				Label label = new Label(page, SWT.NONE);
				label.setForeground(ColorConstants.red);
				label.setText("Could not find class " + this.officeSourceClassName + "\n\n"
						+ ex.getClass().getSimpleName() + ": " + ex.getMessage());
				return;
			}
		}

		// Obtain specification properties for office source
		this.properties = this.officeLoader.loadSpecification(officeSourceClass);

		// Determine if have extension
		if (this.officeSourceExtension != null) {

			// Load page from extension
			try {
				this.officeSourceExtension.createControl(page, this);
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
					OfficeSourceInstance.this.notifyPropertiesChanged();
				}
			});
		}

		// Notify properties changed to set initial state
		this.notifyPropertiesChanged();
	}

	/*
	 * ================== OfficeSourceExtensionContext ======================
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

		// Attempt to load the office type.
		// Issues notified back via the office loader.
		this.loadOfficeType();

		// Flag whether the office type was loaded
		this.context.setOfficeLoaded(this.officeType != null);
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