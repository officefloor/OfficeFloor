/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler();
		compiler.setClassLoader(this.classLoader);
		compiler.setCompilerIssues(this);
		this.sectionLoader = compiler.getSectionLoader();
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
	 * Attempts to load the {@link SectionType}.
	 */
	public void loadSectionType() {

		// Ensure have name
		if (EclipseUtil.isBlank(this.sectionName)) {
			this.sectionType = null;
			this.officeSection = null;
			this.setErrorMessage("Must specify section name");
			return; // must have name
		}

		// Ensure have location
		if (EclipseUtil.isBlank(this.sectionLocation)) {
			this.sectionType = null;
			this.officeSection = null;
			this.setErrorMessage("Must specify section location");
			return; // must have location
		}

		// Ensure have section source class
		if (this.sectionSourceClass == null) {
			return; // page controls not yet loaded
		}

		// Load the section type or office section
		if (this.context.isLoadType()) {
			// Attempt to load the section type
			this.sectionType = this.sectionLoader.loadSectionType(
					this.sectionSourceClass, this.sectionLocation,
					this.properties);
		} else {
			// Attempt to load the office section
			this.officeSection = this.sectionLoader.loadOfficeSection(
					this.sectionName, this.sectionSourceClass,
					this.sectionLocation, this.properties);
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
	 * @param context
	 *            {@link SectionSourceInstanceContext}.
	 */
	@SuppressWarnings("unchecked")
	public void createControls(Composite page) {

		// Obtain the section source class
		if (this.sectionSourceExtension != null) {
			this.sectionSourceClass = this.sectionSourceExtension
					.getSectionSourceClass();
			if (this.sectionSourceClass == null) {
				page.setLayout(new GridLayout());
				Label label = new Label(page, SWT.NONE);
				label.setForeground(ColorConstants.red);
				label.setText("Extension did not provide class "
						+ this.sectionSourceClassName);
				return;
			}
		} else {
			try {
				this.sectionSourceClass = (Class<? extends SectionSource>) this.classLoader
						.loadClass(this.sectionSourceClassName);
			} catch (Throwable ex) {
				page.setLayout(new GridLayout());
				Label label = new Label(page, SWT.NONE);
				label.setForeground(ColorConstants.red);
				label.setText("Could not find class "
						+ this.sectionSourceClassName + "\n\n"
						+ ex.getClass().getSimpleName() + ": "
						+ ex.getMessage());
				return;
			}
		}

		// Obtain specification properties for section source
		this.properties = this.sectionLoader
				.loadSpecification(sectionSourceClass);

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