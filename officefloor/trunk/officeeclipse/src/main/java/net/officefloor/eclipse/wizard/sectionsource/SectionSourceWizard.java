/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.eclipse.wizard.sectionsource;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.eclipse.classpath.ProjectClassLoader;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.extension.ExtensionUtil;
import net.officefloor.eclipse.extension.sectionsource.SectionSourceExtension;
import net.officefloor.eclipse.java.JavaUtil;
import net.officefloor.eclipse.repository.project.ProjectConfigurationContext;
import net.officefloor.eclipse.wizard.WizardUtil;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

/**
 * {@link IWizard} to add and manage {@link OfficeSection} instances.
 * 
 * @author Daniel
 */
public class SectionSourceWizard extends Wizard implements
		SectionSourceInstanceContext {

	/**
	 * Facade method to obtain the {@link SectionInstance}.
	 * 
	 * @param editPart
	 *            {@link AbstractOfficeFloorEditPart} to obtain necessary
	 *            objects to run the {@link SectionSourceWizard}.
	 * @param sectionInstance
	 *            {@link SectionInstance} to based decisions. <code>null</code>
	 *            if creating new {@link SectionInstance}.
	 * @return {@link SectionInstance} or <code>null</code> if cancelled.
	 */
	public static SectionInstance getSectionInstance(
			AbstractOfficeFloorEditPart<?, ?, ?> editPart,
			SectionInstance sectionInstance) {

		// Obtain the project
		IProject project = ProjectConfigurationContext.getProject(editPart
				.getEditor().getEditorInput());

		// Create and run the wizard
		SectionSourceWizard wizard = new SectionSourceWizard(project);
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
	@SuppressWarnings("unchecked")
	public static Map<String, SectionSourceInstance> createSectionSourceInstanceMap(
			ClassLoader classLoader, IProject project,
			SectionSourceInstanceContext context) {

		// Obtain the section source instances (by class name to get unique set)
		Map<String, SectionSourceInstance> sectionSourceInstances = new HashMap<String, SectionSourceInstance>();

		// Obtain from project class path
		try {
			// Obtain the types on the class path
			IType[] types = JavaUtil.getSubTypes(project, SectionSource.class
					.getName());
			for (IType type : types) {
				String className = type.getFullyQualifiedName();
				sectionSourceInstances.put(className,
						new SectionSourceInstance(className, null, classLoader,
								project, context));
			}
		} catch (JavaModelException ex) {
			// Do not add the types
		}

		// Obtain via extension point second to override
		try {
			List<SectionSourceExtension> sectionSourceExtensions = ExtensionUtil
					.createExecutableExtensions(
							SectionSourceExtension.EXTENSION_ID,
							SectionSourceExtension.class);
			for (SectionSourceExtension sectionSourceExtension : sectionSourceExtensions) {
				Class<?> sectionSourceClass = sectionSourceExtension
						.getSectionSourceClass();
				String sectionSourceClassName = sectionSourceClass.getName();
				sectionSourceInstances.put(sectionSourceClassName,
						new SectionSourceInstance(sectionSourceClassName,
								sectionSourceExtension, classLoader, project,
								context));
			}
		} catch (Exception ex) {
			// Do not add the types
		}

		// Return section source instances by the section source class name
		return sectionSourceInstances;
	}

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
	 * {@link SectionSourceNameWizardPage}.
	 */
	private final SectionSourceNameWizardPage tasksPage;

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
	 * @param project
	 *            {@link IProject}.
	 */
	public SectionSourceWizard(IProject project) {
		this(project, null);
	}

	/**
	 * Initiate.
	 * 
	 * @param project
	 *            {@link IProject}.
	 * @param sectionInstance
	 *            {@link SectionInstance} to be edited, or <code>null</code> to
	 *            create a new {@link SectionInstance}.
	 */
	public SectionSourceWizard(IProject project, SectionInstance sectionInstance) {

		// Obtain the class loader for the project
		ProjectClassLoader classLoader = ProjectClassLoader.create(project);

		// Obtain the map of section source instances
		Map<String, SectionSourceInstance> sectionSourceInstanceMap = createSectionSourceInstanceMap(
				classLoader, project, this);

		// Obtain the listing of section source instances (in order)
		SectionSourceInstance[] sectionSourceInstanceListing = sectionSourceInstanceMap
				.values().toArray(new SectionSourceInstance[0]);
		Arrays.sort(sectionSourceInstanceListing,
				new Comparator<SectionSourceInstance>() {
					@Override
					public int compare(SectionSourceInstance a,
							SectionSourceInstance b) {
						return a.getSectionSourceClassName().compareTo(
								b.getSectionSourceClassName());
					}
				});

		// Create the pages
		this.listingPage = new SectionSourceListingWizardPage(
				sectionSourceInstanceListing, project);
		for (SectionSourceInstance sectionSourceInstance : sectionSourceInstanceListing) {
			this.propertiesPages.put(sectionSourceInstance,
					new SectionSourcePropertiesWizardPage(this,
							sectionSourceInstance));
		}
		this.tasksPage = new SectionSourceNameWizardPage();
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
			this.addPage(this.propertiesPages.values().toArray(
					new IWizardPage[0])[0]);
		}
		this.addPage(this.tasksPage);
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		// Handle based on current page
		if (page == this.listingPage) {
			// Listing page, so obtain properties page based on selection
			this.selectedSectionSourceInstance = this.listingPage
					.getSelectedSectionSourceInstance();
			this.currentPropertiesPage = this.propertiesPages
					.get(this.selectedSectionSourceInstance);

			// Activate the page
			this.currentPropertiesPage.activatePage();

			// Load section type to set state and return as next page
			return this.currentPropertiesPage;

		} else if (page instanceof SectionSourcePropertiesWizardPage) {
			// Properties specified, so now select tasks
			this.tasksPage
					.loadSectionSourceInstance(this.selectedSectionSourceInstance);
			return this.tasksPage;

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

		// Ensure have current properties page and is complete
		if (this.currentPropertiesPage == null) {
			return false;
		}
		if (!this.currentPropertiesPage.isPageComplete()) {
			return false;
		}

		// Ensure the tasks page complete
		if (!this.tasksPage.isPageComplete()) {
			return false;
		}

		// All pages complete, may finish
		return true;
	}

	@Override
	public boolean performFinish() {

		// Obtain the details of the section instance
		String sectionName = this.tasksPage.getSectionName();
		String sectionSourceClassName = this.selectedSectionSourceInstance
				.getSectionSourceClassName();
		String sectionLocation = this.selectedSectionSourceInstance
				.getSectionLocation();
		PropertyList propertyList = this.selectedSectionSourceInstance
				.getPropertyList();
		SectionType sectionType = this.selectedSectionSourceInstance
				.getSectionType();

		// Specify the section instance
		this.sectionInstance = new SectionInstance(sectionName,
				sectionSourceClassName, sectionLocation, propertyList,
				sectionType);

		// Finished
		return true;
	}

	/*
	 * ================== SectionSourceInstanceContext ======================
	 */

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
	public void setSectionTypeLoaded(boolean isSectionTypeLoaded) {
		if (this.currentPropertiesPage != null) {
			this.currentPropertiesPage.setPageComplete(isSectionTypeLoaded);
		}
	}

}