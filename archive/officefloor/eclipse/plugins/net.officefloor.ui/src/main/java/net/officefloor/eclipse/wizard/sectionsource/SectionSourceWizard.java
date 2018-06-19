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

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.OfficeSectionType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.eclipse.classpath.ProjectClassLoader;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.configuration.project.ProjectConfigurationContext;
import net.officefloor.eclipse.extension.ExtensionUtil;
import net.officefloor.eclipse.extension.sectionsource.SectionSourceExtension;
import net.officefloor.eclipse.util.JavaUtil;
import net.officefloor.eclipse.util.LogUtil;
import net.officefloor.eclipse.wizard.WizardUtil;

/**
 * {@link IWizard} to add and manage {@link OfficeSection} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionSourceWizard extends Wizard implements SectionSourceInstanceContext {

	/**
	 * Facade method to obtain the {@link SectionInstance} containing the loaded
	 * {@link SectionType}.
	 * 
	 * @param editPart
	 *            {@link AbstractOfficeFloorEditPart} to obtain necessary
	 *            objects to run the {@link SectionSourceWizard}.
	 * @param sectionInstance
	 *            {@link SectionInstance} to based decisions. <code>null</code>
	 *            if creating new {@link SectionInstance}.
	 * @param isAutoWire
	 *            Flag indicating if configuring for auto-wire.
	 * @return {@link SectionInstance} or <code>null</code> if cancelled.
	 */
	public static SectionInstance loadSectionType(AbstractOfficeFloorEditPart<?, ?, ?> editPart,
			SectionInstance sectionInstance, boolean isAutoWire) {
		return getSectionInstance(true, editPart, sectionInstance, isAutoWire);
	}

	/**
	 * Facade method to obtain the {@link SectionInstance} containing the loaded
	 * {@link OfficeSection}.
	 * 
	 * @param editPart
	 *            {@link AbstractOfficeFloorEditPart} to obtain necessary
	 *            objects to run the {@link SectionSourceWizard}.
	 * @param sectionInstance
	 *            {@link SectionInstance} to based decisions. <code>null</code>
	 *            if creating new {@link SectionInstance}.
	 * @param isAutoWire
	 *            Flag indicating if configuring for auto-wire.
	 * @return {@link SectionInstance} or <code>null</code> if cancelled.
	 */
	public static SectionInstance loadOfficeSection(AbstractOfficeFloorEditPart<?, ?, ?> editPart,
			SectionInstance sectionInstance, boolean isAutoWire) {
		return getSectionInstance(false, editPart, sectionInstance, isAutoWire);
	}

	/**
	 * Facade method to obtain the {@link SectionInstance}.
	 * 
	 * @param isLoadType
	 *            Flag indicating if loading only the type.
	 * @param editPart
	 *            {@link AbstractOfficeFloorEditPart} to obtain necessary
	 *            objects to run the {@link SectionSourceWizard}.
	 * @param sectionInstance
	 *            {@link SectionInstance} to based decisions. <code>null</code>
	 *            if creating new {@link SectionInstance}.
	 * @param isAutoWire
	 *            Flag indicating if configuring for auto-wire.
	 * @return {@link SectionInstance} or <code>null</code> if cancelled.
	 */
	public static SectionInstance getSectionInstance(boolean isLoadType, AbstractOfficeFloorEditPart<?, ?, ?> editPart,
			SectionInstance sectionInstance, boolean isAutoWire) {

		// Obtain the project
		IProject project = ProjectConfigurationContext.getProject(editPart.getEditor().getEditorInput());

		// Create and run the wizard
		SectionSourceWizard wizard = new SectionSourceWizard(isLoadType, project, sectionInstance, isAutoWire);
		if (WizardUtil.runWizard(wizard, editPart)) {
			// Successful so return the section instance
			return wizard.getSectionInstance();
		} else {
			// Cancelled so no instance
			return null;
		}
	}

	/**
	 * Creates the mapping of {@link SectionSource} class name to its
	 * {@link SectionSourceInstance}.
	 * 
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param project
	 *            {@link IProject}.
	 * @param context
	 *            {@link SectionSourceInstanceContext}.
	 * @return Mapping of {@link SectionSource} class name to its
	 *         {@link SectionSourceInstance}.
	 */
	@SuppressWarnings("rawtypes")
	public static Map<String, SectionSourceInstance> createSectionSourceInstanceMap(ClassLoader classLoader,
			IProject project, SectionSourceInstanceContext context) {

		// Obtain the section source instances (by class name to get unique set)
		Map<String, SectionSourceInstance> sectionSourceInstances = new HashMap<String, SectionSourceInstance>();

		// Obtain from project class path
		try {
			// Obtain the types on the class path
			IType[] types = JavaUtil.getSubTypes(project, SectionSource.class.getName());
			for (IType type : types) {
				String className = type.getFullyQualifiedName();
				if (ExtensionUtil.isIgnoreSource(className, classLoader)) {
					continue; // ignore source
				}
				sectionSourceInstances.put(className,
						new SectionSourceInstance(className, null, classLoader, project, context));
			}
		} catch (Throwable ex) {
			LogUtil.logError("Failed to obtain java types from project class path", ex);
		}

		// Obtain via extension point second to override
		for (SectionSourceExtension sectionSourceExtension : ExtensionUtil.createSectionSourceExtensionList()) {
			try {
				Class<?> sectionSourceClass = sectionSourceExtension.getSectionSourceClass();
				String sectionSourceClassName = sectionSourceClass.getName();
				sectionSourceInstances.put(sectionSourceClassName, new SectionSourceInstance(sectionSourceClassName,
						sectionSourceExtension, classLoader, project, context));
			} catch (Throwable ex) {
				LogUtil.logError("Failed to create source instance for " + sectionSourceExtension.getClass().getName(),
						ex);
			}
		}

		// Return section source instances by the section source class name
		return sectionSourceInstances;
	}

	/**
	 * Flag indicating to load {@link SectionType} rather than
	 * {@link OfficeSection}.
	 */
	private final boolean isLoadType;

	/**
	 * {@link SectionSourceListingWizardPage}.
	 */
	private final SectionSourceListingWizardPage listingPage;

	/**
	 * {@link SectionSourcePropertiesWizardPage} pages by their
	 * {@link SectionSourceInstance}.
	 */
	private final Map<SectionSourceInstance, SectionSourcePropertiesWizardPage> propertiesPages = new HashMap<SectionSourceInstance, SectionSourcePropertiesWizardPage>();

	/**
	 * {@link SectionSourceAlignOfficeSectionWizardPage}.
	 */
	private final SectionSourceAlignOfficeSectionWizardPage officeSectionAlignPage;

	/**
	 * Selected {@link SectionSourceInstance}.
	 */
	private SectionSourceInstance selectedSectionSourceInstance = null;

	/**
	 * Current {@link SectionSourcePropertiesWizardPage}.
	 */
	private SectionSourcePropertiesWizardPage currentPropertiesPage = null;

	/**
	 * {@link SectionInstance}.
	 */
	private SectionInstance sectionInstance = null;

	/**
	 * Initiate to create a new {@link SectionInstance}.
	 * 
	 * @param isLoadType
	 *            Flag indicating to load {@link SectionType} rather than
	 *            {@link OfficeSection}.
	 * @param project
	 *            {@link IProject}.
	 * @param isAutoWire
	 *            Flag indicating if configuring for auto-wire.
	 */
	public SectionSourceWizard(boolean isLoadType, IProject project, boolean isAutoWire) {
		this(isLoadType, project, null, isAutoWire);
	}

	/**
	 * Initiate.
	 * 
	 * @param isLoadType
	 *            Flag indicating to load {@link SectionType} rather than
	 *            {@link OfficeSection}.
	 * @param project
	 *            {@link IProject}.
	 * @param sectionInstance
	 *            {@link SectionInstance} to be edited, or <code>null</code> to
	 *            create a new {@link SectionInstance}.
	 * @param isAutoWire
	 *            Flag indicating if configuring for auto-wire.
	 */
	public SectionSourceWizard(boolean isLoadType, IProject project, SectionInstance sectionInstance,
			boolean isAutoWire) {
		this.isLoadType = isLoadType;

		// Obtain the class loader for the project
		ProjectClassLoader classLoader = ProjectClassLoader.create(project);

		// Obtain the map of section source instances
		Map<String, SectionSourceInstance> sectionSourceInstanceMap = createSectionSourceInstanceMap(classLoader,
				project, this);

		// Obtain the listing of section source instances (in order)
		SectionSourceInstance[] sectionSourceInstanceListing = sectionSourceInstanceMap.values()
				.toArray(new SectionSourceInstance[0]);
		Arrays.sort(sectionSourceInstanceListing, new Comparator<SectionSourceInstance>() {
			@Override
			public int compare(SectionSourceInstance a, SectionSourceInstance b) {
				return a.getSectionSourceClassName().compareTo(b.getSectionSourceClassName());
			}
		});

		// Create the pages
		this.listingPage = new SectionSourceListingWizardPage(sectionSourceInstanceListing, project, sectionInstance);
		for (SectionSourceInstance sectionSourceInstance : sectionSourceInstanceListing) {
			this.propertiesPages.put(sectionSourceInstance,
					new SectionSourcePropertiesWizardPage(this, sectionSourceInstance));
		}

		// Determine if require creating refactor pages
		if (sectionInstance != null) {
			// Refactoring section
			this.officeSectionAlignPage = new SectionSourceAlignOfficeSectionWizardPage(sectionInstance, isLoadType,
					isAutoWire);

			// Load section instance for matching section source instance
			String sectionSourceClassName = sectionInstance.getSectionSourceClassName();
			for (SectionSourceInstance sectionSourceInstance : sectionSourceInstanceListing) {
				if (sectionSourceClassName.equals(sectionSourceInstance.getSectionSourceClassName())) {
					sectionSourceInstance.loadSectionInstance(sectionInstance);
				}
			}

		} else {
			// Creating new section
			this.officeSectionAlignPage = null;
		}
	}

	/**
	 * Obtains the {@link SectionInstance}.
	 * 
	 * @return {@link SectionInstance}.
	 */
	public SectionInstance getSectionInstance() {
		return this.sectionInstance;
	}

	/*
	 * ====================== Wizard ==================================
	 */

	@Override
	public void addPages() {
		this.addPage(this.listingPage);
		if (this.propertiesPages.size() > 0) {
			// Load the first properties page
			this.addPage(this.propertiesPages.values().toArray(new IWizardPage[0])[0]);
		}

		// Add refactor pages
		if (this.officeSectionAlignPage != null) {
			this.addPage(this.officeSectionAlignPage);
		}
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		// Handle based on current page
		if (page == this.listingPage) {
			// Listing page, so obtain properties page based on selection
			this.selectedSectionSourceInstance = this.listingPage.getSelectedSectionSourceInstance();
			this.currentPropertiesPage = this.propertiesPages.get(this.selectedSectionSourceInstance);

			// Load section type to set state and return as next page
			if (!this.listingPage.isClassSectionSource()) {
				// Not ClassSectionSource, so activate properties page
				this.currentPropertiesPage.activatePage();

				// Obtain properties
				return this.currentPropertiesPage;
			} else {
				// Skip properties page for ClassSectionSource
				page = this.currentPropertiesPage;
			}
		}

		if (page == this.currentPropertiesPage) {
			// Determine if refactoring
			if (this.officeSectionAlignPage != null) {
				// Refactoring office section
				this.officeSectionAlignPage.loadSectionSourceInstance(this.selectedSectionSourceInstance);
				return this.officeSectionAlignPage;
			}

			// Not refactoring, nothing further
			return null;

		} else {
			// Tasks selected, nothing further
			return null;
		}
	}

	@Override
	public boolean canFinish() {

		// Ensure listing page complete
		if (!this.listingPage.isPageComplete()) {
			return false;
		}

		// Properties only if NOT ClassSectionSource
		if (!this.listingPage.isClassSectionSource()) {
			// Ensure have current properties page and is complete
			if (this.currentPropertiesPage == null) {
				return false;
			}
			if (!this.currentPropertiesPage.isPageComplete()) {
				return false;
			}
		}

		// All pages complete, may finish
		return true;
	}

	@Override
	public boolean performFinish() {

		// Obtain the details of the section instance
		String sectionName = this.selectedSectionSourceInstance.getSectionName();
		String sectionSourceClassName = this.selectedSectionSourceInstance.getSectionSourceClassName();
		String sectionLocation = this.selectedSectionSourceInstance.getSectionLocation();
		PropertyList propertyList = this.selectedSectionSourceInstance.getPropertyList();
		SectionType sectionType = this.selectedSectionSourceInstance.getSectionType();
		OfficeSectionType officeSectionType = this.selectedSectionSourceInstance.getOfficeSectionType();

		// Obtain the mappings
		Map<String, String> inputNameMapping = null;
		Map<String, String> outputNameMapping = null;
		Map<String, String> objectNameMapping = null;
		if (this.officeSectionAlignPage != null) {
			// Obtain mappings for office section
			inputNameMapping = this.officeSectionAlignPage.getInputNameMapping();
			outputNameMapping = this.officeSectionAlignPage.getOutputNameMapping();
			objectNameMapping = this.officeSectionAlignPage.getObjectNameMapping();
		}

		// Normalise the properties
		propertyList.normalise();

		// Specify the section instance
		this.sectionInstance = new SectionInstance(sectionName, sectionSourceClassName, sectionLocation, propertyList,
				sectionType, officeSectionType, inputNameMapping, outputNameMapping, objectNameMapping);

		// Finished
		return true;
	}

	/*
	 * ================== SectionSourceInstanceContext ======================
	 */

	@Override
	public boolean isLoadType() {
		return this.isLoadType;
	}

	@Override
	public void setTitle(String title) {
		if (this.currentPropertiesPage != null) {
			this.currentPropertiesPage.setTitle(title);
		}
	}

	@Override
	public void setErrorMessage(String message) {
		if (this.currentPropertiesPage != null) {
			this.currentPropertiesPage.setErrorMessage(message);
		}
	}

	@Override
	public void setSectionLoaded(boolean isSectionTypeLoaded) {
		if (this.currentPropertiesPage != null) {
			this.currentPropertiesPage.setPageComplete(isSectionTypeLoaded);
		}
	}

}