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
package net.officefloor.eclipse.wizard.access;

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
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.common.dialog.input.InputAdapter;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.impl.PropertyListInput;
import net.officefloor.eclipse.extension.access.HttpSecuritySourceExtension;
import net.officefloor.eclipse.extension.access.HttpSecuritySourceExtensionContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.plugin.web.http.security.HttpSecuritySource;
import net.officefloor.plugin.web.http.security.type.HttpSecuritySourceSpecificationRunnable;
import net.officefloor.plugin.web.http.security.type.HttpSecurityType;
import net.officefloor.plugin.web.http.security.type.HttpSecurityTypeRunnable;

/**
 * {@link HttpSecuritySource} instance.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecuritySourceInstance extends AbstractCompilerIssues implements HttpSecuritySourceExtensionContext {

	/**
	 * Fully qualified class name of the {@link HttpSecuritySource}.
	 */
	private final String httpSecuritySourceClassName;

	/**
	 * {@link HttpSecuritySourceExtension}. May be <code>null</code> if not
	 * obtained via extension point.
	 */
	private final HttpSecuritySourceExtension<?> httpSecuritySourceExtension;

	/**
	 * {@link ClassLoader}.
	 */
	private final ClassLoader classLoader;

	/**
	 * {@link IProject}.
	 */
	private final IProject project;

	/**
	 * {@link HttpSecuritySourceInstanceContext}.
	 */
	private final HttpSecuritySourceInstanceContext context;

	/**
	 * {@link AccessInstance}.
	 */
	private AccessInstance accessInstance;

	/**
	 * Authentication timeout.
	 */
	private long authenticationTimeout;

	/**
	 * {@link PropertyList}.
	 */
	private PropertyList properties;

	/**
	 * {@link HttpSecurityType}.
	 */
	private HttpSecurityType<?, ?, ?, ?> httpSecurityType;

	/**
	 * Initiate.
	 * 
	 * @param httpSecuritySourceClassName
	 *            Fully qualified class name of the {@link HttpSecuritySource}.
	 * @param httpSecuritySourceExtension
	 *            {@link HttpSecuritySourceExtension}. May be <code>null</code>.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param project
	 *            {@link IProject}.
	 * @param context
	 *            {@link HttpSecuritySourceInstanceContext}.
	 */
	HttpSecuritySourceInstance(String httpSecuritySourceClassName,
			HttpSecuritySourceExtension<?> httpSecuritySourceExtension, ClassLoader classLoader, IProject project,
			HttpSecuritySourceInstanceContext context) {
		this.httpSecuritySourceClassName = httpSecuritySourceClassName;
		this.httpSecuritySourceExtension = httpSecuritySourceExtension;
		this.classLoader = classLoader;
		this.project = project;
		this.context = context;
	}

	/**
	 * Specifies the authentication timeout.
	 * 
	 * @param authenticationTimeout
	 *            Authentication timeout.
	 */
	public void setAuthenticationTimeout(long authenticationTimeout) {
		this.authenticationTimeout = authenticationTimeout;
	}

	/**
	 * Loads the particular {@link AccessInstance} for this
	 * {@link HttpSecuritySourceInstance} to configure properties from.
	 * 
	 * @param accessInstance
	 *            {@link AccessInstance}.
	 */
	public void loadAccessInstance(AccessInstance accessInstance) {
		this.accessInstance = accessInstance;
	}

	/**
	 * Attempts to load the {@link HttpSecurityType}.
	 */
	public void loadHttpSecurityType() {
		this.loadHttpSecurityType(this);
	}

	/**
	 * Attempts to load the {@link HttpSecurityType}.
	 * 
	 * @param issues
	 *            {@link CompilerIssues} to be notified of issues in loading.
	 * @return <code>true</code> if loaded.
	 */
	public boolean loadHttpSecurityType(CompilerIssues issues) {

		// Create the compiler
		OfficeFloorCompiler compiler = this.createOfficeFloorCompiler(issues);

		// Obtain the HTTP Security Source class.
		// (Always attempt to obtain to provide details on page)
		Class<?> httpSecuritySourceClass = this.loadHttpSecuritySourceClass(null, issues, compiler);
		if (httpSecuritySourceClass == null) {
			return false; // did not load HttpSecuritySource class
		}

		// Ensure have properties
		if (this.properties == null) {
			this.properties = OfficeFloorCompiler.newPropertyList();
		}

		// Attempt to load the HTTP Security type
		try {
			this.httpSecurityType = HttpSecurityTypeRunnable.loadHttpSecurityType(httpSecuritySourceClass.getName(),
					this.properties, compiler);
		} catch (Throwable ex) {
			// Failed to load properties
			this.context.setErrorMessage(ex.getMessage() + " (" + ex.getClass().getSimpleName() + ")");
			return false; // must load type
		}

		// Return indicating if loaded
		return (this.httpSecurityType != null);
	}

	/**
	 * Obtains the label for the {@link HttpSecuritySource}.
	 * 
	 * @return Label for the {@link HttpSecuritySource}.
	 */
	public String getHttpSecuritySourceLabel() {
		if (this.httpSecuritySourceExtension == null) {
			// No extension so use class name
			return this.httpSecuritySourceClassName;
		} else {
			// Attempt to obtain from extension
			String name = this.httpSecuritySourceExtension.getHttpSecuritySourceLabel();
			if (EclipseUtil.isBlank(name)) {
				// No name so use class name
				name = this.httpSecuritySourceClassName;
			}
			return name;
		}
	}

	/**
	 * Obtains the fully qualified class name of the {@link HttpSecuritySource}.
	 * 
	 * @return {@link HttpSecuritySource} class name.
	 */
	public String getHttpSecuritySourceClassName() {
		return this.httpSecuritySourceClassName;
	}

	/**
	 * Obtains the authentication timeout.
	 * 
	 * @return Authentication timeout.
	 */
	public long getAuthenticationTimeout() {
		return this.authenticationTimeout;
	}

	/**
	 * Obtains the {@link PropertyList} to configure the
	 * {@link HttpSecuritySource}.
	 * 
	 * @return Populated {@link PropertyList}.
	 */
	public PropertyList getPropertyList() {
		return this.properties;
	}

	/**
	 * Obtains the loaded {@link HttpSecurityType}.
	 * 
	 * @return Loaded {@link HttpSecurityType} or <code>null</code> if issue
	 *         loading.
	 */
	public HttpSecurityType<?, ?, ?, ?> getHttpSecurityType() {
		return this.httpSecurityType;
	}

	/**
	 * Creates the {@link Control} instances to populate the {@link Property}
	 * instances.
	 * 
	 * @param page
	 *            {@link Composite} to add {@link Control} instances.
	 */
	public void createControls(Composite page) {

		// Obtain the HTTP Security Source class.
		// (Always attempt to obtain to provide details on page)
		Class<?> httpSecuritySourceClass = this.loadHttpSecuritySourceClass(page, null, null);
		if (httpSecuritySourceClass == null) {
			return; // did not load HttpSecuritySource class
		}

		// Obtain specification properties for HTTP Security source
		OfficeFloorCompiler compiler = this.createOfficeFloorCompiler(this);
		try {
			this.properties = HttpSecuritySourceSpecificationRunnable
					.loadSpecification(httpSecuritySourceClass.getName(), compiler);
		} catch (Throwable ex) {
			// Failed to load properties
			this.context.setErrorMessage(ex.getMessage() + " (" + ex.getClass().getSimpleName() + ")");
			return; // must load properties
		}

		// Load Access instance properties if available
		if (this.accessInstance != null) {
			for (Property property : this.accessInstance.getPropertylist()) {
				this.properties.getOrAddProperty(property.getName()).setValue(property.getValue());
			}
		}

		// Determine if have extension
		if (this.httpSecuritySourceExtension != null) {

			// Load page from extension
			try {
				this.httpSecuritySourceExtension.createControl(page, this);
			} catch (Throwable ex) {
				// Failed to load page
				this.context.setErrorMessage(ex.getMessage() + " (" + ex.getClass().getSimpleName() + ")");
			}

		} else {
			// Not an extension so provide properties table to fill out
			page.setLayout(new GridLayout());
			PropertyListInput propertyListInput = new PropertyListInput(this.properties);
			new InputHandler<PropertyList>(page, propertyListInput, new InputAdapter() {
				@Override
				public void notifyValueChanged(Object value) {
					HttpSecuritySourceInstance.this.notifyPropertiesChanged();
				}
			});
		}

		// Notify properties changed to set initial state
		this.notifyPropertiesChanged();
	}

	/**
	 * Loads the {@link HttpSecuritySource} class.
	 * 
	 * @param page
	 *            {@link Composite} to provide error if unable to load.
	 * @param issues
	 *            {@link CompilerIssues} to provide error if unable to load.
	 * @param node
	 *            {@link Node} to report the issues against.
	 * @return {@link HttpSecuritySource} class or <code>null</code> if not able
	 *         to load.
	 */
	private Class<?> loadHttpSecuritySourceClass(Composite page, CompilerIssues issues, Node node) {

		// Obtain the HTTP Security source class
		String errorMessage = null;
		Class<?> httpSecuritySourceClass = null;
		if (this.httpSecuritySourceExtension != null) {
			httpSecuritySourceClass = this.httpSecuritySourceExtension.getHttpSecuritySourceClass();
			if (httpSecuritySourceClass == null) {
				errorMessage = "Extension did not provide class " + this.httpSecuritySourceClassName;
			}
		} else {
			try {
				httpSecuritySourceClass = (Class<?>) this.classLoader.loadClass(this.httpSecuritySourceClassName);
			} catch (Throwable ex) {
				errorMessage = "Could not find class " + this.httpSecuritySourceClassName + "\n\n"
						+ ex.getClass().getSimpleName() + ": " + ex.getMessage();
			}
		}

		// Handle error
		if (!EclipseUtil.isBlank(errorMessage)) {
			if (page != null) {
				// Provide error to page
				page.setLayout(new GridLayout());
				Label label = new Label(page, SWT.NONE);
				label.setForeground(ColorConstants.red);
				label.setText(errorMessage);
			} else if (issues != null) {
				// Provide error to issues
				issues.addIssue(node, errorMessage);
			} else {
				throw new IllegalStateException("Must provide either Page or CompilerIssues");
			}
		}

		// Return the HTTP Security Source class
		return httpSecuritySourceClass;
	}

	/**
	 * Creates the {@link OfficeFloorCompiler}.
	 * 
	 * @param issues
	 *            {@link CompilerIssues}.
	 * @return {@link OfficeFloorCompiler}.
	 */
	private OfficeFloorCompiler createOfficeFloorCompiler(CompilerIssues issues) {
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(this.classLoader);
		compiler.setCompilerIssues(issues);
		return compiler;
	}

	/*
	 * ================ HttpSecuritySourceExtensionContext ====================
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

		// Attempt to load the HTTP Security type.
		// Issues notified back via the HTTP Security loader.
		this.loadHttpSecurityType();

		// Flag whether loaded
		this.context.setHttpSecurityTypeLoaded(this.httpSecurityType != null);
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