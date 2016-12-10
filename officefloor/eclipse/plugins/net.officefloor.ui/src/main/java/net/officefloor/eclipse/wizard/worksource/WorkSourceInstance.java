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
package net.officefloor.eclipse.wizard.worksource;

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
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceProperty;
import net.officefloor.compile.work.WorkLoader;
import net.officefloor.compile.work.WorkType;
import net.officefloor.eclipse.common.dialog.input.InputAdapter;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.impl.PropertyListInput;
import net.officefloor.eclipse.extension.worksource.WorkSourceExtension;
import net.officefloor.eclipse.extension.worksource.WorkSourceExtensionContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.frame.api.execute.Work;

/**
 * {@link WorkSource} instance.
 * 
 * @author Daniel Sagenschneider
 */
public class WorkSourceInstance extends AbstractCompilerIssues implements WorkSourceExtensionContext {

	/**
	 * Fully qualified class name of the {@link WorkSource}.
	 */
	private final String workSourceClassName;

	/**
	 * {@link WorkSourceExtension}. May be <code>null</code> if not obtained via
	 * extension point.
	 */
	private final WorkSourceExtension<?, ?> workSourceExtension;

	/**
	 * {@link WorkLoader}.
	 */
	private final WorkLoader workLoader;

	/**
	 * {@link ClassLoader}.
	 */
	private final ClassLoader classLoader;

	/**
	 * {@link IProject}.
	 */
	private final IProject project;

	/**
	 * {@link WorkSourceInstanceContext}.
	 */
	private final WorkSourceInstanceContext context;

	/**
	 * {@link WorkInstance}.
	 */
	private WorkInstance workInstance;

	/**
	 * {@link WorkSource} class.
	 */
	@SuppressWarnings("rawtypes")
	private Class workSourceClass;

	/**
	 * {@link PropertyList}.
	 */
	private PropertyList properties;

	/**
	 * {@link WorkType}.
	 */
	private WorkType<?> workType;

	/**
	 * Initiate.
	 * 
	 * @param workSourceClassName
	 *            Fully qualified class name of the {@link WorkSource}.
	 * @param workSourceExtension
	 *            {@link WorkSourceExtension}. May be <code>null</code>.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param project
	 *            {@link IProject}.
	 * @param context
	 *            {@link WorkSourceInstanceContext}.
	 */
	WorkSourceInstance(String workSourceClassName, WorkSourceExtension<?, ?> workSourceExtension,
			ClassLoader classLoader, IProject project, WorkSourceInstanceContext context) {
		this.workSourceClassName = workSourceClassName;
		this.workSourceExtension = workSourceExtension;
		this.classLoader = classLoader;
		this.project = project;
		this.context = context;

		// Obtain the work loader
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(this.classLoader);
		compiler.setCompilerIssues(this);
		this.workLoader = compiler.getWorkLoader();
	}

	/**
	 * Loads the particular {@link WorkInstance} for this
	 * {@link WorkSourceInstance} to configure properties from.
	 * 
	 * @param workInstance
	 *            {@link WorkInstance}.
	 */
	public void loadWorkInstance(WorkInstance workInstance) {
		this.workInstance = workInstance;
	}

	/**
	 * Attempts to load the {@link WorkType}.
	 */
	@SuppressWarnings("unchecked")
	public void loadWorkType() {
		// Attempt to load the work type
		this.workType = this.workLoader.loadWorkType(this.workSourceClass, this.properties);
	}

	/**
	 * Obtains the label for the {@link WorkSource}.
	 * 
	 * @return Label for the {@link WorkSource}.
	 */
	public String getWorkSourceLabel() {
		if (this.workSourceExtension == null) {
			// No extension so use class name
			return this.workSourceClassName;
		} else {
			// Attempt to obtain from extension
			String name = this.workSourceExtension.getWorkSourceLabel();
			if (EclipseUtil.isBlank(name)) {
				// No name so use class name
				name = this.workSourceClassName;
			}
			return name;
		}
	}

	/**
	 * Obtains the fully qualified class name of the {@link WorkSource}.
	 * 
	 * @return {@link WorkSource} class name.
	 */
	public String getWorkSourceClassName() {
		return this.workSourceClassName;
	}

	/**
	 * Obtains the {@link PropertyList} to source the {@link Work} from the
	 * {@link WorkSource}.
	 * 
	 * @return Populated {@link PropertyList}.
	 */
	public PropertyList getPropertyList() {
		return this.properties;
	}

	/**
	 * Obtains the loaded {@link WorkType}.
	 * 
	 * @return Loaded {@link WorkType} or <code>null</code> if issue loading.
	 */
	public WorkType<?> getWorkType() {
		return this.workType;
	}

	/**
	 * Obtains the suggested name for the {@link Work}.
	 * 
	 * @return Suggested name for the {@link Work}.
	 */
	public String getSuggestedWorkName() {

		// Ensure have extension
		if (this.workSourceExtension == null) {
			return ""; // no suggestion
		}

		// Return the suggested name
		return this.workSourceExtension.getSuggestedWorkName(this.properties);
	}

	/**
	 * Creates the {@link Control} instances to populate the
	 * {@link WorkSourceProperty} instances.
	 * 
	 * @param page
	 *            {@link Composite} to add {@link Control} instances.
	 */
	@SuppressWarnings("unchecked")
	public void createControls(Composite page) {

		// Obtain the work source class
		if (this.workSourceExtension != null) {
			this.workSourceClass = this.workSourceExtension.getWorkSourceClass();
			if (this.workSourceClass == null) {
				page.setLayout(new GridLayout());
				Label label = new Label(page, SWT.NONE);
				label.setForeground(ColorConstants.red);
				label.setText("Extension did not provide class " + this.workSourceClassName);
				return;
			}
		} else {
			try {
				this.workSourceClass = (Class<? extends WorkSource<?>>) this.classLoader
						.loadClass(this.workSourceClassName);
			} catch (Throwable ex) {
				page.setLayout(new GridLayout());
				Label label = new Label(page, SWT.NONE);
				label.setForeground(ColorConstants.red);
				label.setText("Could not find class " + this.workSourceClassName + "\n\n"
						+ ex.getClass().getSimpleName() + ": " + ex.getMessage());
				return;
			}
		}

		// Obtain specification properties for work source
		try {
			this.properties = this.workLoader.loadSpecification(this.workSourceClass);
		} catch (Throwable ex) {
			// Failed to find/instantiate the class with project class loader
			page.setLayout(new GridLayout());
			Label label = new Label(page, SWT.NONE);
			label.setForeground(ColorConstants.red);
			label.setText("Could not find class " + this.workSourceClass.getName() + "\n\n"
					+ ex.getClass().getSimpleName() + ": " + ex.getMessage());
			return;
		}

		// Load work instance properties if available
		if (this.workInstance != null) {
			for (Property property : this.workInstance.getPropertyList()) {
				this.properties.getOrAddProperty(property.getName()).setValue(property.getValue());
			}
		}

		// Determine if have extension
		if (this.workSourceExtension != null) {

			// Load page from extension
			try {
				this.workSourceExtension.createControl(page, this);
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
					WorkSourceInstance.this.notifyPropertiesChanged();
				}
			});
		}

		// Notify properties changed to set initial state
		this.notifyPropertiesChanged();
	}

	/*
	 * ================== WorkSourceExtensionContext ======================
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

		// Attempt to load the work type.
		// Issues notified back via the work loader.
		this.loadWorkType();

		// Flag whether the work type was loaded
		this.context.setWorkTypeLoaded(this.workType != null);
	}

	@Override
	public IProject getProject() {
		return this.project;
	}

	@Override
	public ClassLoader getClassLoader() {
		return this.classLoader;
	}

	/*
	 * ===================== CompilerIssues ===============================
	 */

	@Override
	protected void handleDefaultIssue(DefaultCompilerIssue issue) {
		this.context.setErrorMessage(CompileException.toIssueString(issue));
	}

}