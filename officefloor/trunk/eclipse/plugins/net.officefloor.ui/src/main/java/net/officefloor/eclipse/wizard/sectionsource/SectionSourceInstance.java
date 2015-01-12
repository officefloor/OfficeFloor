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
package net.officefloor.eclipse.wizard.sectionsource;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionLoader;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.eclipse.common.dialog.input.InputAdapter;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.common.dialog.input.impl.PropertyListInput;
import net.officefloor.eclipse.extension.sectionsource.SectionSourceExtension;
import net.officefloor.eclipse.extension.sectionsource.SectionSourceExtensionContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.plugin.section.clazz.ClassSectionSource;

import org.eclipse.core.resources.IProject;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * {@link SectionSource} instance.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionSourceInstance implements SectionSourceExtensionContext,
		CompilerIssues {

	/**
	 * Fully qualified class name of the {@link SectionSource}.
	 */
	private final String sectionSourceClassName;

	/**
	 * {@link SectionSourceExtension}. May be <code>null</code> if not obtained
	 * via extension point.
	 */
	private final SectionSourceExtension<?> sectionSourceExtension;

	/**
	 * {@link SectionLoader}.
	 */
	private final SectionLoader sectionLoader;

	/**
	 * {@link ClassLoader}.
	 */
	private final ClassLoader classLoader;

	/**
	 * {@link IProject}.
	 */
	private final IProject project;

	/**
	 * {@link SectionSourceInstanceContext}.
	 */
	private final SectionSourceInstanceContext context;

	/**
	 * {@link SectionInstance}.
	 */
	private SectionInstance sectionInstance;

	/**
	 * {@link SectionSource} class.
	 */
	private Class<? extends SectionSource> sectionSourceClass;

	/**
	 * {@link PropertyList}.
	 */
	private PropertyList properties;

	/**
	 * {@link SectionType}.
	 */
	private SectionType sectionType;

	/**
	 * {@link OfficeSection}.
	 */
	private OfficeSection officeSection;

	/**
	 * Name of the {@link OfficeSection}.
	 */
	private String sectionName;

	/**
	 * Location of the {@link OfficeSection}.
	 */
	private String sectionLocation;

	/**
	 * Initiate.
	 * 
	 * @param sectionSourceClassName
	 *            Fully qualified class name of the {@link SectionSource}.
	 * @param sectionSourceExtension
	 *            {@link SectionSourceExtension}. May be <code>null</code>.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param project
	 *            {@link IProject}.
	 * @param context
	 *            {@link SectionSourceInstanceContext}.
	 */
	SectionSourceInstance(String sectionSourceClassName,
			SectionSourceExtension<?> sectionSourceExtension,
			ClassLoader classLoader, IProject project,
			SectionSourceInstanceContext context) {
		this.sectionSourceClassName = sectionSourceClassName;
		this.sectionSourceExtension = sectionSourceExtension;
		this.classLoader = classLoader;
		this.project = project;
		this.context = context;

		// Obtain the section loader
		this.sectionLoader = this.createSectionLoader(this);
	}

	/**
	 * Specifies the location of the {@link OfficeSection}.
	 * 
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection}.
	 */
	public void setSectionNameAndLocation(String sectionName,
			String sectionLocation) {
		this.sectionName = sectionName;
		this.sectionLocation = sectionLocation;

		// Notify properties changed as now have location
		this.notifyPropertiesChanged();
	}

	/**
	 * Loads the particular {@link SectionInstance} for this
	 * {@link SectionSourceInstance} to configure properties from.
	 * 
	 * @param sectionInstance
	 *            {@link SectionInstance}.
	 */
	public void loadSectionInstance(SectionInstance sectionInstance) {
		this.sectionInstance = sectionInstance;
	}

	/**
	 * Attempts to load the {@link SectionType}.
	 */
	public void loadSectionType() {
		this.loadSectionType(this.sectionLoader, this);
	}

	/**
	 * Attempts to load the {@link SectionType} for the
	 * {@link ClassSectionSource}.
	 * 
	 * @param issues
	 *            {@link CompilerIssues} to be notified of issues in loading.
	 * @return <code>true</code> if loaded.
	 */
	public boolean loadSectionType(CompilerIssues issues) {
		// Create SectionLoader to use issues
		SectionLoader sectionLoader = this.createSectionLoader(issues);

		// Load the Section Type returning whether successful
		return this.loadSectionType(sectionLoader, issues);
	}

	/**
	 * Attempts to load the {@link SectionType}.
	 * 
	 * @param loader
	 *            {@link SectionLoader}.
	 * @param issues
	 *            {@link CompilerIssues}.
	 * @return <code>true</code> if loaded.
	 */
	private boolean loadSectionType(SectionLoader loader, CompilerIssues issues) {

		// Ensure have name
		if (EclipseUtil.isBlank(this.sectionName)) {
			this.sectionType = null;
			this.officeSection = null;
			issues.addIssue(null, null, null, null, "Must specify section name");
			return false; // must have name
		}

		// Ensure have location
		if (EclipseUtil.isBlank(this.sectionLocation)) {
			this.sectionType = null;
			this.officeSection = null;
			issues.addIssue(null, null, null, null,
					"Must specify section location");
			return false; // must have location
		}

		// Ensure have section source class
		if (this.sectionSourceClass == null) {
			// Attempt to load section source class
			if (!this.loadSectionSourceClass(null, issues)) {
				return false; // not able to load section source class
			}
		}

		// Ensure have properties
		if (this.properties == null) {
			this.properties = OfficeFloorCompiler.newPropertyList();
		}

		// Load the section type or office section
		if (this.context.isLoadType()) {
			// Attempt to load the section type
			this.sectionType = loader.loadSectionType(this.sectionSourceClass,
					this.sectionLocation, this.properties);

			// Return indicating if loaded
			return (this.sectionType != null);
		} else {
			// Attempt to load the office section
			this.officeSection = loader.loadOfficeSection(this.sectionName,
					this.sectionSourceClass, this.sectionLocation,
					this.properties);

			// Return indicating if loaded
			return (this.officeSection != null);
		}
	}

	/**
	 * Obtains the label for the {@link SectionSource}.
	 * 
	 * @return Label for the {@link SectionSource}.
	 */
	public String getSectionSourceLabel() {
		if (this.sectionSourceExtension == null) {
			// No extension so use class name
			return this.sectionSourceClassName;
		} else {
			// Attempt to obtain from extension
			String name = this.sectionSourceExtension.getSectionSourceLabel();
			if (EclipseUtil.isBlank(name)) {
				// No name so use class name
				name = this.sectionSourceClassName;
			}
			return name;
		}
	}

	/**
	 * Obtains the fully qualified class name of the {@link SectionSource}.
	 * 
	 * @return {@link SectionSource} class name.
	 */
	public String getSectionSourceClassName() {
		return this.sectionSourceClassName;
	}

	/**
	 * Obtains the name of the {@link OfficeSection}.
	 * 
	 * @return Name of the {@link OfficeSection}.
	 */
	public String getSectionName() {
		return this.sectionName;
	}

	/**
	 * Obtains the location of the {@link OfficeSection}.
	 * 
	 * @return Location of the {@link OfficeSection}.
	 */
	public String getSectionLocation() {
		return this.sectionLocation;
	}

	/**
	 * Obtains the {@link PropertyList} to source the {@link OfficeSection} from
	 * the {@link SectionSource}.
	 * 
	 * @return Populated {@link PropertyList}.
	 */
	public PropertyList getPropertyList() {
		return this.properties;
	}

	/**
	 * Obtains the loaded {@link SectionType}.
	 * 
	 * @return Loaded {@link SectionType} or <code>null</code> if issue loading.
	 */
	public SectionType getSectionType() {
		return this.sectionType;
	}

	/**
	 * Obtains the loaded {@link OfficeSection}.
	 * 
	 * @return Loaded {@link OfficeSection} or <code>null</code> if issue
	 *         loading.
	 */
	public OfficeSection getOfficeSection() {
		return this.officeSection;
	}

	/**
	 * Creates the {@link Control} instances to populate the
	 * {@link SectionLoaderProperty} instances.
	 * 
	 * @param page
	 *            {@link Composite} to add {@link Control} instances.
	 */
	public void createControls(Composite page) {

		// Obtain the section source class.
		// (Always attempt to obtain to provide details on page)
		if (!this.loadSectionSourceClass(page, null)) {
			return; // did not load SectionSource class
		}

		// Obtain specification properties for section source
		this.properties = this.sectionLoader
				.loadSpecification(this.sectionSourceClass);

		// Load section instance properties if available
		if (this.sectionInstance != null) {
			for (Property property : this.sectionInstance.getPropertylist()) {
				this.properties.getOrAddProperty(property.getName()).setValue(
						property.getValue());
			}
		}

		// Determine if have extension
		if (this.sectionSourceExtension != null) {

			// Load page from extension
			try {
				this.sectionSourceExtension.createControl(page, this);
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
							SectionSourceInstance.this
									.notifyPropertiesChanged();
						}
					});
		}

		// Notify properties changed to set initial state
		this.notifyPropertiesChanged();
	}

	/**
	 * Loads the {@link SectionSource} class.
	 * 
	 * @param page
	 *            {@link Composite} to provide error if unable to load.
	 * @param issues
	 *            {@link CompilerIssues} to provide error if unable to load.
	 * @return <code>true</code> if loaded the {@link SectionSource} class.
	 */
	@SuppressWarnings("unchecked")
	private boolean loadSectionSourceClass(Composite page, CompilerIssues issues) {

		// Obtain the section source class
		String errorMessage = null;
		if (this.sectionSourceExtension != null) {
			this.sectionSourceClass = this.sectionSourceExtension
					.getSectionSourceClass();
			if (this.sectionSourceClass == null) {
				errorMessage = "Extension did not provide class "
						+ this.sectionSourceClassName;
			}
		} else {
			try {
				this.sectionSourceClass = (Class<? extends SectionSource>) this.classLoader
						.loadClass(this.sectionSourceClassName);
			} catch (Throwable ex) {
				errorMessage = "Could not find class "
						+ this.sectionSourceClassName + "\n\n"
						+ ex.getClass().getSimpleName() + ": "
						+ ex.getMessage();
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
				issues.addIssue(null, null, null, null, errorMessage);
			} else {
				throw new IllegalStateException(
						"Must provide either Page or CompilerIssues");
			}
		}

		// Return whether loaded
		return (this.sectionSourceClass != null);
	}

	/**
	 * Creates the {@link SectionLoader}.
	 * 
	 * @param issues
	 *            {@link CompilerIssues}.
	 * @return {@link SectionLoader}.
	 */
	private SectionLoader createSectionLoader(CompilerIssues issues) {
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(this.classLoader);
		compiler.setCompilerIssues(issues);
		return compiler.getSectionLoader();
	}

	/*
	 * ================== SectionSourceExtensionContext ======================
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

		// Attempt to load the section type.
		// Issues notified back via the section loader.
		this.loadSectionType();

		// Flag whether the section type or office section was loaded
		if (this.context.isLoadType()) {
			this.context.setSectionLoaded(this.sectionType != null);
		} else {
			this.context.setSectionLoaded(this.officeSection != null);
		}
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