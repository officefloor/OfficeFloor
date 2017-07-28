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
package net.officefloor.eclipse.wizard.governancesource;

import org.eclipse.core.resources.IProject;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.governance.GovernanceLoader;
import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.compile.impl.issues.AbstractCompilerIssues;
import net.officefloor.compile.impl.issues.CompileException;
import net.officefloor.compile.impl.issues.DefaultCompilerIssue;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.eclipse.common.dialog.input.InputAdapter;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.impl.PropertyListInput;
import net.officefloor.eclipse.extension.governancesource.GovernanceSourceExtension;
import net.officefloor.eclipse.extension.governancesource.GovernanceSourceExtensionContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.frame.api.governance.Governance;

/**
 * {@link GovernanceSource} instance.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceSourceInstance extends AbstractCompilerIssues implements GovernanceSourceExtensionContext {

	/**
	 * Fully qualified class name of the {@link GovernanceSource}.
	 */
	private final String governanceSourceClassName;

	/**
	 * {@link GovernanceSourceExtension}. May be <code>null</code> if not
	 * obtained via extension point.
	 */
	private final GovernanceSourceExtension<?, ?, ?> governanceSourceExtension;

	/**
	 * {@link GovernanceLoader}.
	 */
	private final GovernanceLoader governanceLoader;

	/**
	 * {@link ClassLoader}.
	 */
	private final ClassLoader classLoader;

	/**
	 * {@link IProject}.
	 */
	private final IProject project;

	/**
	 * {@link GovernanceSourceInstanceContext}.
	 */
	private final GovernanceSourceInstanceContext context;

	/**
	 * {@link GovernanceInstance}.
	 */
	private GovernanceInstance governanceInstance;

	/**
	 * {@link GovernanceSource} class.
	 */
	private Class<? extends GovernanceSource<?, ?>> governanceSourceClass;

	/**
	 * {@link PropertyList}.
	 */
	private PropertyList properties;

	/**
	 * {@link GovernanceType}.
	 */
	private GovernanceType<?, ?> governanceType;

	/**
	 * Name of the {@link Governance}.
	 */
	private String governanceName;

	/**
	 * Initiate.
	 * 
	 * @param governanceSourceClassName
	 *            Fully qualified class name of the {@link GovernanceSource}.
	 * @param governanceSourceExtension
	 *            {@link GovernanceSourceExtension}. May be <code>null</code>.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param project
	 *            {@link IProject}.
	 * @param context
	 *            {@link GovernanceSourceInstanceContext}.
	 */
	GovernanceSourceInstance(String governanceSourceClassName,
			GovernanceSourceExtension<?, ?, ?> governanceSourceExtension, ClassLoader classLoader, IProject project,
			GovernanceSourceInstanceContext context) {
		this.governanceSourceClassName = governanceSourceClassName;
		this.governanceSourceExtension = governanceSourceExtension;
		this.classLoader = classLoader;
		this.project = project;
		this.context = context;

		// Obtain the governance loader
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(this.classLoader);
		compiler.setCompilerIssues(this);
		this.governanceLoader = compiler.getGovernanceLoader();
	}

	/**
	 * Specifies the name of the {@link Governance}.
	 * 
	 * @param governanceName
	 *            Name of the {@link Governance}.
	 */
	public void setGovernanceName(String governanceName) {
		this.governanceName = governanceName;

		// Notify properties changed as now have location
		this.notifyPropertiesChanged();
	}

	/**
	 * Loads the particular {@link GovernanceInstance} for this
	 * {@link GovernanceSourceInstance} to configure properties from.
	 * 
	 * @param governanceInstance
	 *            {@link GovernanceInstance}.
	 */
	public void loadGovernanceInstance(GovernanceInstance governanceInstance) {
		this.governanceInstance = governanceInstance;
	}

	/**
	 * Attempts to load the {@link GovernanceType}.
	 */
	public void loadGovernanceType() {

		// Ensure have name
		if (EclipseUtil.isBlank(this.governanceName)) {
			this.governanceType = null;
			this.setErrorMessage("Must specify governance name");
			return; // must have name
		}

		// Ensure have governance source class
		if (this.governanceSourceClass == null) {
			return; // page controls not yet loaded
		}

		// Attempt to load the governance type
		this.governanceType = this.governanceLoader.loadGovernanceType(this.governanceSourceClass, this.properties);
	}

	/**
	 * Obtains the label for the {@link GovernanceSource}.
	 * 
	 * @return Label for the {@link GovernanceSource}.
	 */
	public String getGovernanceSourceLabel() {
		if (this.governanceSourceExtension == null) {
			// No extension so use class name
			return this.governanceSourceClassName;
		} else {
			// Attempt to obtain from extension
			String name = this.governanceSourceExtension.getGovernanceSourceLabel();
			if (EclipseUtil.isBlank(name)) {
				// No name so use class name
				name = this.governanceSourceClassName;
			}
			return name;
		}
	}

	/**
	 * Obtains the fully qualified class name of the {@link GovernanceSource}.
	 * 
	 * @return {@link GovernanceSource} class name.
	 */
	public String getGovernanceSourceClassName() {
		return this.governanceSourceClassName;
	}

	/**
	 * Obtains the name of the {@link Governance}.
	 * 
	 * @return Name of the {@link Governance}.
	 */
	public String getGovernanceName() {
		return this.governanceName;
	}

	/**
	 * Obtains the {@link PropertyList} to source the {@link Governance} from
	 * the {@link GovernanceSource}.
	 * 
	 * @return Populated {@link PropertyList}.
	 */
	public PropertyList getPropertyList() {
		return this.properties;
	}

	/**
	 * Obtains the loaded {@link GovernanceType}.
	 * 
	 * @return Loaded {@link GovernanceType} or <code>null</code> if issue
	 *         loading.
	 */
	public GovernanceType<?, ?> getGovernanceType() {
		return this.governanceType;
	}

	/**
	 * Creates the {@link Control} instances to populate the
	 * {@link GovernanceSource} {@link Property} instances.
	 * 
	 * @param page
	 *            {@link Composite} to add {@link Control} instances.
	 */
	@SuppressWarnings("unchecked")
	public void createControls(Composite page) {

		// Obtain the governance source class
		if (this.governanceSourceExtension != null) {
			this.governanceSourceClass = (Class<? extends GovernanceSource<?, ?>>) this.governanceSourceExtension
					.getGovernanceSourceClass();
			if (this.governanceSourceClass == null) {
				page.setLayout(new GridLayout());
				Label label = new Label(page, SWT.NONE);
				label.setForeground(ColorConstants.red);
				label.setText("Extension did not provide class " + this.governanceSourceClassName);
				return;
			}
		} else {
			try {
				this.governanceSourceClass = (Class<? extends GovernanceSource<?, ?>>) this.classLoader
						.loadClass(this.governanceSourceClassName);
			} catch (Throwable ex) {
				page.setLayout(new GridLayout());
				Label label = new Label(page, SWT.NONE);
				label.setForeground(ColorConstants.red);
				label.setText("Could not find class " + this.governanceSourceClassName + "\n\n"
						+ ex.getClass().getSimpleName() + ": " + ex.getMessage());
				return;
			}
		}

		// Obtain specification properties for governance source
		this.properties = this.governanceLoader.loadSpecification(this.governanceSourceClass);

		// Load governance instance properties if available
		if (this.governanceInstance != null) {
			for (Property property : this.governanceInstance.getPropertyList()) {
				this.properties.getOrAddProperty(property.getName()).setValue(property.getValue());
			}
		}

		// Determine if have extension
		if (this.governanceSourceExtension != null) {

			// Load page from extension
			try {
				this.governanceSourceExtension.createControl(page, this);
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
					GovernanceSourceInstance.this.notifyPropertiesChanged();
				}
			});
		}

		// Notify properties changed to set initial state
		this.notifyPropertiesChanged();
	}

	/*
	 * ================== GovernanceSourceExtensionContext =================
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

		// Attempt to load the governance type.
		// Issues notified back via the governance loader.
		this.loadGovernanceType();

		// Flag whether the governance type was loaded
		this.context.setGovernanceTypeLoaded(this.governanceType != null);
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