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
package net.officefloor.eclipse.wizard.managedobjectsource;

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
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.classpath.ProjectClassLoader;
import net.officefloor.eclipse.common.dialog.input.InputAdapter;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.impl.PropertyListInput;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtension;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtensionContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceProperty;

/**
 * {@link ManagedObjectSource} instance.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectSourceInstance extends AbstractCompilerIssues implements ManagedObjectSourceExtensionContext {

	/**
	 * Fully qualified class name of the {@link ManagedObjectSource}.
	 */
	private final String managedObjectSourceClassName;

	/**
	 * {@link ManagedObjectSourceExtension}. May be <code>null</code> if not
	 * obtained via extension point.
	 */
	private final ManagedObjectSourceExtension<?, ?, ?> managedObjectSourceExtension;

	/**
	 * {@link ManagedObjectLoader}.
	 */
	private final ManagedObjectLoader managedObjectLoader;

	/**
	 * {@link ClassLoader}.
	 */
	private final ClassLoader classLoader;

	/**
	 * {@link IProject}.
	 */
	private final IProject project;

	/**
	 * {@link ManagedObjectSourceInstanceContext}.
	 */
	private final ManagedObjectSourceInstanceContext context;

	/**
	 * {@link ManagedObjectSource} class.
	 */
	@SuppressWarnings("rawtypes")
	private Class managedObjectSourceClass;

	/**
	 * {@link PropertyList}.
	 */
	private PropertyList properties;

	/**
	 * {@link ManagedObjectType}.
	 */
	private ManagedObjectType<?> managedObjectType;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectSourceClassName
	 *            Fully qualified class name of the {@link ManagedObjectSource}.
	 * @param managedObjectSourceExtension
	 *            {@link ManagedObjectSourceExtension}. <code>null</code> if no
	 *            {@link ManagedObjectSourceExtension} and using
	 *            {@link ManagedObjectSource}.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param project
	 *            {@link IProject}.
	 * @param context
	 *            {@link ManagedObjectSourceInstanceContext}.
	 */
	ManagedObjectSourceInstance(String managedObjectSourceClassName,
			ManagedObjectSourceExtension<?, ?, ?> managedObjectSourceExtension, ClassLoader classLoader,
			IProject project, ManagedObjectSourceInstanceContext context) {
		this.managedObjectSourceClassName = managedObjectSourceClassName;
		this.managedObjectSourceExtension = managedObjectSourceExtension;
		this.classLoader = classLoader;
		this.project = project;
		this.context = context;

		// Obtain the managed object loader
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(this.classLoader);
		compiler.setCompilerIssues(this);
		this.managedObjectLoader = compiler.getManagedObjectLoader();
	}

	/**
	 * Attempts to load the {@link ManagedObjectType}.
	 */
	@SuppressWarnings("unchecked")
	public void loadManagedObjectType() {

		// Ensure have managed object source class
		if (this.managedObjectSourceClass == null) {
			return; // page controls not yet loaded
		}

		// Ensure have properties
		if (this.properties == null) {
			return; // failed to load specification
		}

		// Attempt to load the managed object type
		this.managedObjectType = this.managedObjectLoader.loadManagedObjectType(this.managedObjectSourceClass,
				this.properties);
	}

	/**
	 * Obtains the label for the {@link ManagedObjectSource}.
	 * 
	 * @return Label for the {@link ManagedObjectSource}.
	 */
	public String getManagedObjectSourceLabel() {
		if (this.managedObjectSourceExtension == null) {
			// No extension so use class name
			return this.managedObjectSourceClassName;
		} else {
			// Attempt to obtain from extension
			String name = this.managedObjectSourceExtension.getManagedObjectSourceLabel();
			if (EclipseUtil.isBlank(name)) {
				// No name so use class name
				name = this.managedObjectSourceClassName;
			}
			return name;
		}
	}

	/**
	 * Obtains the fully qualified class name of the {@link ManagedObjectSource}
	 * .
	 * 
	 * @return {@link ManagedObjectSource} class name.
	 */
	public String getManagedObjectSourceClassName() {
		return this.managedObjectSourceClassName;
	}

	/**
	 * Obtains the {@link PropertyList} to source the {@link ManagedObject} from
	 * the {@link ManagedObjectSource}.
	 * 
	 * @return Populated {@link PropertyList}.
	 */
	public PropertyList getPropertyList() {
		return this.properties;
	}

	/**
	 * Obtains the loaded {@link ManagedObjectType}.
	 * 
	 * @return Loaded {@link ManagedObjectType} or <code>null</code> if issue
	 *         loading.
	 */
	public ManagedObjectType<?> getManagedObjectType() {
		return this.managedObjectType;
	}

	/**
	 * Obtains the suggested name for the {@link ManagedObject}.
	 * 
	 * @return Suggested name for the {@link ManagedObject}.
	 */
	public String getSuggestedManagedObjectName() {

		// Ensure have extension
		if (this.managedObjectSourceExtension == null) {
			return ""; // no suggestion
		}

		// Return the suggested name
		return this.managedObjectSourceExtension.getSuggestedManagedObjectSourceName(this.properties);
	}

	/**
	 * Creates the {@link Control} instances to populate the
	 * {@link ManagedObjectSourceProperty} instances.
	 * 
	 * @param page
	 *            {@link Composite} to add {@link Control} instances.
	 */
	@SuppressWarnings("unchecked")
	public void createControls(Composite page) {

		// Obtain the managed object source class
		if (this.managedObjectSourceExtension != null) {
			this.managedObjectSourceClass = this.managedObjectSourceExtension.getManagedObjectSourceClass();
			if (this.managedObjectSourceClass == null) {
				page.setLayout(new GridLayout());
				Label label = new Label(page, SWT.NONE);
				label.setForeground(ColorConstants.red);
				label.setText("Extension did not provide class " + this.managedObjectSourceClassName);
				return;
			}
		} else {
			try {
				this.managedObjectSourceClass = (Class<? extends ManagedObjectSource<?, ?>>) this.classLoader
						.loadClass(this.managedObjectSourceClassName);
			} catch (Throwable ex) {
				page.setLayout(new GridLayout());
				Label label = new Label(page, SWT.NONE);
				label.setForeground(ColorConstants.red);
				label.setText("Could not find class " + this.managedObjectSourceClassName + "\n\n"
						+ ex.getClass().getSimpleName() + ": " + ex.getMessage());
				return;
			}
		}

		// Obtain specification properties for managed object source
		this.properties = this.managedObjectLoader.loadSpecification(this.managedObjectSourceClass);

		// Determine if have extension
		if (this.managedObjectSourceExtension != null) {

			// Load page from extension
			try {
				this.managedObjectSourceExtension.createControl(page, this);
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
					ManagedObjectSourceInstance.this.notifyPropertiesChanged();
				}
			});
		}

		// Notify properties changed to set initial state
		this.notifyPropertiesChanged();
	}

	/*
	 * ============== ManagedObjectSourceExtensionContext ======================
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

		// Attempt to load the managed object type.
		// Issues notified back via the managed object loader.
		this.loadManagedObjectType();

		// Flag whether the managed object type was loaded
		this.context.setManagedObjectTypeLoaded(this.managedObjectType != null);
	}

	@Override
	public IProject getProject() {
		return this.project;
	}

	@Override
	public ClassLoader getClassLoader() {
		return ProjectClassLoader.create(this.project);
	}

	/*
	 * ===================== CompilerIssues ===============================
	 */

	@Override
	protected void handleDefaultIssue(DefaultCompilerIssue issue) {
		this.context.setErrorMessage(CompileException.toIssueString(issue));
	}

}