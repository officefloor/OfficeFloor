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
package net.officefloor.eclipse.wizard.managedfunctionsource;

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
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedfunction.ManagedFunctionLoader;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceProperty;
import net.officefloor.eclipse.common.dialog.input.InputAdapter;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.impl.PropertyListInput;
import net.officefloor.eclipse.extension.managedfunctionsource.ManagedFunctionSourceExtension;
import net.officefloor.eclipse.extension.managedfunctionsource.ManagedFunctionSourceExtensionContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.model.section.FunctionNamespaceModel;

/**
 * {@link ManagedFunctionSource} instance.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionSourceInstance extends AbstractCompilerIssues
		implements ManagedFunctionSourceExtensionContext {

	/**
	 * Fully qualified class name of the {@link ManagedFunctionSource}.
	 */
	private final String managedFunctionSourceClassName;

	/**
	 * {@link ManagedFunctionSourceExtension}. May be <code>null</code> if not
	 * obtained via extension point.
	 */
	private final ManagedFunctionSourceExtension<?> managedFunctionSourceExtension;

	/**
	 * {@link ManagedFunctionLoader}.
	 */
	private final ManagedFunctionLoader managedFunctionLoader;

	/**
	 * {@link ClassLoader}.
	 */
	private final ClassLoader classLoader;

	/**
	 * {@link IProject}.
	 */
	private final IProject project;

	/**
	 * {@link ManagedFunctionSourceInstanceContext}.
	 */
	private final ManagedFunctionSourceInstanceContext context;

	/**
	 * {@link FunctionNamespaceInstance}.
	 */
	private FunctionNamespaceInstance namespaceInstance;

	/**
	 * {@link ManagedFunctionSource} class.
	 */
	@SuppressWarnings("rawtypes")
	private Class managedFunctionSourceClass;

	/**
	 * {@link PropertyList}.
	 */
	private PropertyList properties;

	/**
	 * {@link FunctionNamespaceType}.
	 */
	private FunctionNamespaceType namespaceType;

	/**
	 * Initiate.
	 * 
	 * @param managedFunctionSourceClassName
	 *            Fully qualified class name of the
	 *            {@link ManagedFunctionSource}.
	 * @param managedFunctionSourceExtension
	 *            {@link ManagedFunctionSourceExtension}. May be
	 *            <code>null</code>.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param project
	 *            {@link IProject}.
	 * @param context
	 *            {@link ManagedFunctionSourceInstanceContext}.
	 */
	ManagedFunctionSourceInstance(String managedFunctionSourceClassName,
			ManagedFunctionSourceExtension<?> managedFunctionSourceExtension, ClassLoader classLoader, IProject project,
			ManagedFunctionSourceInstanceContext context) {
		this.managedFunctionSourceClassName = managedFunctionSourceClassName;
		this.managedFunctionSourceExtension = managedFunctionSourceExtension;
		this.classLoader = classLoader;
		this.project = project;
		this.context = context;

		// Obtain the work loader
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(this.classLoader);
		compiler.setCompilerIssues(this);
		this.managedFunctionLoader = compiler.getManagedFunctionLoader();
	}

	/**
	 * Loads the particular {@link FunctionNamespaceInstance} for this
	 * {@link ManagedFunctionSourceInstance} to configure properties from.
	 * 
	 * @param namespaceInstance
	 *            {@link FunctionNamespaceInstance}.
	 */
	public void loadFunctionNamespaceInstance(FunctionNamespaceInstance namespaceInstance) {
		this.namespaceInstance = namespaceInstance;
	}

	/**
	 * Attempts to load the {@link FunctionNamespaceType}.
	 */
	@SuppressWarnings("unchecked")
	public void loadFunctionNamespaceType() {
		// Attempt to load the namespace type
		this.namespaceType = this.managedFunctionLoader.loadManagedFunctionType(this.managedFunctionSourceClass,
				this.properties);
	}

	/**
	 * Obtains the label for the {@link ManagedFunctionSource}.
	 * 
	 * @return Label for the {@link ManagedFunctionSource}.
	 */
	public String getManagedFunctionSourceLabel() {
		if (this.managedFunctionSourceExtension == null) {
			// No extension so use class name
			return this.managedFunctionSourceClassName;
		} else {
			// Attempt to obtain from extension
			String name = this.managedFunctionSourceExtension.getManagedFunctionSourceLabel();
			if (EclipseUtil.isBlank(name)) {
				// No name so use class name
				name = this.managedFunctionSourceClassName;
			}
			return name;
		}
	}

	/**
	 * Obtains the fully qualified class name of the
	 * {@link ManagedFunctionSource}.
	 * 
	 * @return {@link ManagedFunctionSource} class name.
	 */
	public String getManagedFunctionSourceClassName() {
		return this.managedFunctionSourceClassName;
	}

	/**
	 * Obtains the {@link PropertyList} to source the {@link ManagedFunction}
	 * from the {@link ManagedFunctionSource}.
	 * 
	 * @return Populated {@link PropertyList}.
	 */
	public PropertyList getPropertyList() {
		return this.properties;
	}

	/**
	 * Obtains the loaded {@link FunctionNamespaceType}.
	 * 
	 * @return Loaded {@link FunctionNamespaceType} or <code>null</code> if
	 *         issue loading.
	 */
	public FunctionNamespaceType getFunctionNamespaceType() {
		return this.namespaceType;
	}

	/**
	 * Obtains the suggested name for the {@link FunctionNamespaceModel}.
	 * 
	 * @return Suggested name for the {@link FunctionNamespaceModel}.
	 */
	public String getSuggestedFunctionNamespaceName() {

		// Ensure have extension
		if (this.managedFunctionSourceExtension == null) {
			return ""; // no suggestion
		}

		// Return the suggested name
		return this.managedFunctionSourceExtension.getSuggestedFunctionNamespaceName(this.properties);
	}

	/**
	 * Creates the {@link Control} instances to populate the
	 * {@link ManagedFunctionSourceProperty} instances.
	 * 
	 * @param page
	 *            {@link Composite} to add {@link Control} instances.
	 */
	@SuppressWarnings("unchecked")
	public void createControls(Composite page) {

		// Obtain the managed function source class
		if (this.managedFunctionSourceExtension != null) {
			this.managedFunctionSourceClass = this.managedFunctionSourceExtension.getManagedFunctionSourceClass();
			if (this.managedFunctionSourceClass == null) {
				page.setLayout(new GridLayout());
				Label label = new Label(page, SWT.NONE);
				label.setForeground(ColorConstants.red);
				label.setText("Extension did not provide class " + this.managedFunctionSourceClassName);
				return;
			}
		} else {
			try {
				this.managedFunctionSourceClass = (Class<? extends ManagedFunctionSource>) this.classLoader
						.loadClass(this.managedFunctionSourceClassName);
			} catch (Throwable ex) {
				page.setLayout(new GridLayout());
				Label label = new Label(page, SWT.NONE);
				label.setForeground(ColorConstants.red);
				label.setText("Could not find class " + this.managedFunctionSourceClassName + "\n\n"
						+ ex.getClass().getSimpleName() + ": " + ex.getMessage());
				return;
			}
		}

		// Obtain specification properties for managed function source
		try {
			this.properties = this.managedFunctionLoader.loadSpecification(this.managedFunctionSourceClass);
		} catch (Throwable ex) {
			// Failed to find/instantiate the class with project class loader
			page.setLayout(new GridLayout());
			Label label = new Label(page, SWT.NONE);
			label.setForeground(ColorConstants.red);
			label.setText("Could not find class " + this.managedFunctionSourceClass.getName() + "\n\n"
					+ ex.getClass().getSimpleName() + ": " + ex.getMessage());
			return;
		}

		// Load namespace instance properties if available
		if (this.namespaceInstance != null) {
			for (Property property : this.namespaceInstance.getPropertyList()) {
				this.properties.getOrAddProperty(property.getName()).setValue(property.getValue());
			}
		}

		// Determine if have extension
		if (this.managedFunctionSourceExtension != null) {

			// Load page from extension
			try {
				this.managedFunctionSourceExtension.createControl(page, this);
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
					ManagedFunctionSourceInstance.this.notifyPropertiesChanged();
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

		// Attempt to load the namespace type.
		// Issues notified back via the managed function loader.
		this.loadFunctionNamespaceType();

		// Flag whether the namespace type was loaded
		this.context.setFunctionNamespaceTypeLoaded(this.namespaceType != null);
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