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

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.part.EditorPart;

import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.eclipse.classpath.ProjectClassLoader;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.configuration.project.ProjectConfigurationContext;
import net.officefloor.eclipse.extension.ExtensionUtil;
import net.officefloor.eclipse.extension.managedfunctionsource.ManagedFunctionSourceExtension;
import net.officefloor.eclipse.util.JavaUtil;
import net.officefloor.eclipse.util.LogUtil;
import net.officefloor.eclipse.wizard.WizardUtil;
import net.officefloor.frame.api.function.ManagedFunction;

/**
 * {@link IWizard} to add and manage {@link ManagedFunction} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionSourceWizard extends Wizard implements ManagedFunctionSourceInstanceContext {

	/**
	 * Facade method to obtain the {@link FunctionNamespaceInstance}.
	 * 
	 * @param editPart
	 *            {@link AbstractOfficeFloorEditPart} to obtain necessary
	 *            objects to run the {@link ManagedFunctionSourceWizard}.
	 * @param namespaceInstance
	 *            {@link FunctionNamespaceInstance} to based decisions.
	 *            <code>null</code> if creating new
	 *            {@link FunctionNamespaceInstance}.
	 * @return {@link FunctionNamespaceInstance} or <code>null</code> if
	 *         cancelled.
	 */
	public static FunctionNamespaceInstance getFunctionNamespaceInstance(AbstractOfficeFloorEditPart<?, ?, ?> editPart,
			FunctionNamespaceInstance namespaceInstance) {

		// Create and run the wizard
		ManagedFunctionSourceWizard wizard = new ManagedFunctionSourceWizard(editPart, namespaceInstance);
		if (WizardUtil.runWizard(wizard, editPart)) {
			// Successful so return the namespace instance
			return wizard.getFunctionNamespaceInstance();
		} else {
			// Cancelled so no instance
			return null;
		}
	}

	/**
	 * Creates the mapping of {@link ManagedFunctionSource} class name to its
	 * {@link ManagedFunctionSourceInstance}.
	 * 
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param project
	 *            {@link IProject}.
	 * @param context
	 *            {@link ManagedFunctionSourceInstanceContext}.
	 * @return Mapping of {@link ManagedFunctionSource} class name to its
	 *         {@link ManagedFunctionSourceInstance}.
	 */
	@SuppressWarnings("rawtypes")
	public static Map<String, ManagedFunctionSourceInstance> createManagedFunctionSourceInstanceMap(
			ClassLoader classLoader, IProject project, ManagedFunctionSourceInstanceContext context) {

		// Obtain managed function source instances (class name for unique set)
		Map<String, ManagedFunctionSourceInstance> managedFunctionSourceInstances = new HashMap<String, ManagedFunctionSourceInstance>();

		// Obtain from project class path
		try {
			// Obtain the types on the class path
			IType[] types = JavaUtil.getSubTypes(project, ManagedFunctionSource.class.getName());
			for (IType type : types) {
				String className = type.getFullyQualifiedName();
				if (ExtensionUtil.isIgnoreSource(className, classLoader)) {
					continue; // ignore source
				}
				managedFunctionSourceInstances.put(className,
						new ManagedFunctionSourceInstance(className, null, classLoader, project, context));
			}
		} catch (Throwable ex) {
			LogUtil.logError("Failed to obtain java types from project class path", ex);
		}

		// Obtain via extension point second to override
		for (ManagedFunctionSourceExtension managedFunctionSourceExtension : ExtensionUtil
				.createWorkSourceExtensionList()) {
			try {
				Class<?> managedFunctionSourceClass = managedFunctionSourceExtension.getManagedFunctionSourceClass();
				String managedFunctionSourceClassName = managedFunctionSourceClass.getName();
				managedFunctionSourceInstances.put(managedFunctionSourceClassName, new ManagedFunctionSourceInstance(
						managedFunctionSourceClassName, managedFunctionSourceExtension, classLoader, project, context));
			} catch (Throwable ex) {
				LogUtil.logError(
						"Failed to create source instance for " + managedFunctionSourceExtension.getClass().getName(),
						ex);
			}
		}

		// Return managed function source instances by their class name
		return managedFunctionSourceInstances;
	}

	/**
	 * {@link ManagedFunctionSourceListingWizardPage}.
	 */
	private final ManagedFunctionSourceListingWizardPage listingPage;

	/**
	 * {@link ManagedFunctionSourcePropertiesWizardPage} pages by their
	 * {@link ManagedFunctionSourceInstance}.
	 */
	private final Map<ManagedFunctionSourceInstance, ManagedFunctionSourcePropertiesWizardPage> propertiesPages = new HashMap<ManagedFunctionSourceInstance, ManagedFunctionSourcePropertiesWizardPage>();

	/**
	 * {@link ManagedFunctionSourceFunctionsWizardPage}.
	 */
	private final ManagedFunctionSourceFunctionsWizardPage functionsPage;

	/**
	 * Selected {@link ManagedFunctionSourceInstance}.
	 */
	private ManagedFunctionSourceInstance selectedWorkSourceInstance = null;

	/**
	 * Current {@link ManagedFunctionSourcePropertiesWizardPage}.
	 */
	private ManagedFunctionSourcePropertiesWizardPage currentPropertiesPage = null;

	/**
	 * {@link ManagedFunctionSourceAlignFunctionsWizardPage}.
	 */
	private final ManagedFunctionSourceAlignFunctionsWizardPage alignFunctionsPage;

	/**
	 * {@link ManagedFunctionSourceAlignObjectsWizardPage}.
	 */
	private final ManagedFunctionSourceAlignObjectsWizardPage alignObjectsPage;

	/**
	 * {@link ManagedFunctionSourceAlignFlowsEscalationsWizardPage}.
	 */
	private final ManagedFunctionSourceAlignFlowsEscalationsWizardPage alignFlowsEscalationsPage;

	/**
	 * {@link FunctionNamespaceInstance}.
	 */
	private FunctionNamespaceInstance namespaceInstance = null;

	/**
	 * Initiate to create a new {@link FunctionNamespaceInstance}.
	 * 
	 * @param editPart
	 *            {@link AbstractOfficeFloorEditPart}.
	 */
	public ManagedFunctionSourceWizard(AbstractOfficeFloorEditPart<?, ?, ?> editPart) {
		this(editPart, null);
	}

	/**
	 * Initiate.
	 * 
	 * @param editPart
	 *            {@link AbstractOfficeFloorEditPart}.
	 * @param namespaceInstance
	 *            {@link FunctionNamespaceInstance} to be edited, or
	 *            <code>null</code> to create a new
	 *            {@link FunctionNamespaceInstance}.
	 */
	public ManagedFunctionSourceWizard(AbstractOfficeFloorEditPart<?, ?, ?> editPart,
			FunctionNamespaceInstance namespaceInstance) {

		// Obtain the project
		EditorPart editorPart = editPart.getEditor();
		IProject project = ProjectConfigurationContext.getProject(editorPart.getEditorInput());

		// Obtain the class loader for the project
		ProjectClassLoader classLoader = ProjectClassLoader.create(project);

		// Obtain the map of managed function source instances
		Map<String, ManagedFunctionSourceInstance> managedFunctionSourceInstanceMap = createManagedFunctionSourceInstanceMap(
				classLoader, project, this);

		// Obtain the listing of managed function source instances (in order)
		ManagedFunctionSourceInstance[] managedFunctionSourceInstanceListing = managedFunctionSourceInstanceMap.values()
				.toArray(new ManagedFunctionSourceInstance[0]);
		Arrays.sort(managedFunctionSourceInstanceListing, new Comparator<ManagedFunctionSourceInstance>() {
			@Override
			public int compare(ManagedFunctionSourceInstance a, ManagedFunctionSourceInstance b) {
				return a.getManagedFunctionSourceClassName().compareTo(b.getManagedFunctionSourceClassName());
			}
		});

		// Create the pages
		this.listingPage = new ManagedFunctionSourceListingWizardPage(managedFunctionSourceInstanceListing,
				namespaceInstance);
		for (ManagedFunctionSourceInstance managedFunctionSourceInstance : managedFunctionSourceInstanceListing) {
			this.propertiesPages.put(managedFunctionSourceInstance,
					new ManagedFunctionSourcePropertiesWizardPage(this, managedFunctionSourceInstance));
		}
		this.functionsPage = new ManagedFunctionSourceFunctionsWizardPage();

		// Add pages to refactor namespace
		if (namespaceInstance != null) {
			// Refactoring namespace
			this.alignFunctionsPage = new ManagedFunctionSourceAlignFunctionsWizardPage(namespaceInstance);
			this.alignObjectsPage = new ManagedFunctionSourceAlignObjectsWizardPage(namespaceInstance);
			this.alignFlowsEscalationsPage = new ManagedFunctionSourceAlignFlowsEscalationsWizardPage(
					namespaceInstance);

			// Load namespace instance against managed function source instance
			String managedFunctionSourceClassName = namespaceInstance.getManagedFunctionSourceClassName();
			for (ManagedFunctionSourceInstance workSourceInstance : managedFunctionSourceInstanceListing) {
				if (managedFunctionSourceClassName.equals(workSourceInstance.getManagedFunctionSourceClassName())) {
					workSourceInstance.loadFunctionNamespaceInstance(namespaceInstance);
				}
			}

		} else {
			// Create new namespace (no refactoring required)
			this.alignFunctionsPage = null;
			this.alignObjectsPage = null;
			this.alignFlowsEscalationsPage = null;
		}
	}

	/**
	 * Obtains the {@link FunctionNamespaceInstance}.
	 * 
	 * @return {@link FunctionNamespaceInstance}.
	 */
	public FunctionNamespaceInstance getFunctionNamespaceInstance() {
		return this.namespaceInstance;
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
		this.addPage(this.functionsPage);
		if (this.alignFunctionsPage != null) {
			// Add refactoring pages
			this.addPage(this.alignFunctionsPage);
			this.addPage(this.alignObjectsPage);
			this.addPage(this.alignFlowsEscalationsPage);
		}
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {

		// Handle based on current page
		if (page == this.listingPage) {
			// Listing page, so obtain properties page based on selection
			this.selectedWorkSourceInstance = this.listingPage.getSelectedManagedFunctionSourceInstance();
			this.currentPropertiesPage = this.propertiesPages.get(this.selectedWorkSourceInstance);

			// Load function namespace type to set state and return as next page
			return this.currentPropertiesPage;

		} else if (page instanceof ManagedFunctionSourcePropertiesWizardPage) {
			// Properties specified, so now select functions
			this.functionsPage.loadManagedFunctionSourceInstance(this.selectedWorkSourceInstance);
			return this.functionsPage;

		} else if (page == this.functionsPage) {
			// Return align functions (or null if new namespace)
			if (this.alignFunctionsPage != null) {
				this.alignFunctionsPage.loadWorkSourceInstance(this.selectedWorkSourceInstance);
			}
			return this.alignFunctionsPage;

		} else if (page == this.alignFunctionsPage) {
			// Return align objects
			this.alignObjectsPage.loadManagedFunctionMappingAndManagedFunctionSourceInstance(
					this.alignFunctionsPage.getManagedFunctionNameMapping(), this.selectedWorkSourceInstance);
			return this.alignObjectsPage;

		} else if (page == this.alignObjectsPage) {
			// Return align flows/escalations
			this.alignFlowsEscalationsPage.loadWorkTaskMappingAndWorkSourceInstance(
					this.alignFunctionsPage.getManagedFunctionNameMapping(), this.selectedWorkSourceInstance);
			return this.alignFlowsEscalationsPage;

		}

		// If here, then should be nothing further
		return null;
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

		// Ensure the functions page complete
		if (!this.functionsPage.isPageComplete()) {
			return false;
		}

		// All pages complete, may finish
		return true;
	}

	@Override
	public boolean performFinish() {

		// Obtain the details of the namespace instance
		String namespaceName = this.functionsPage.getFunctionNamespaceName();
		String managedFunctionSourceClassName = this.selectedWorkSourceInstance.getManagedFunctionSourceClassName();
		PropertyList propertyList = this.selectedWorkSourceInstance.getPropertyList();
		FunctionNamespaceType namespaceType = this.selectedWorkSourceInstance.getFunctionNamespaceType();
		ManagedFunctionType<?, ?>[] functionTypes = this.functionsPage.getSelectedManagedFunctionTypes()
				.toArray(new ManagedFunctionType[0]);

		// Obtain the refactor alignments
		Map<String, String> managedFunctionNameMapping = (this.alignFunctionsPage == null ? null
				: this.alignFunctionsPage.getManagedFunctionNameMapping());
		Map<String, Map<String, String>> managedFunctionObjectNameMappingForManagedFunction = (this.alignObjectsPage == null
				? null : this.alignObjectsPage.getManagedFunctionObjectNameMappingForManagedFunction());
		Map<String, Map<String, String>> functionFlowNameMappingForFunction = (this.alignFlowsEscalationsPage == null
				? null : this.alignFlowsEscalationsPage.getFunctionFlowNameMappingForFunction());
		Map<String, Map<String, String>> functionEscalationTypeMappingForFunction = (this.alignFlowsEscalationsPage == null
				? null : this.alignFlowsEscalationsPage.getFunctionEscalationTypeMappingForFunction());

		// Normalise the properties
		propertyList.normalise();

		// Specify the namespace instance
		this.namespaceInstance = new FunctionNamespaceInstance(namespaceName, managedFunctionSourceClassName,
				propertyList, namespaceType, functionTypes, managedFunctionNameMapping,
				managedFunctionObjectNameMappingForManagedFunction, functionFlowNameMappingForFunction,
				functionEscalationTypeMappingForFunction);

		// Finished
		return true;
	}

	/*
	 * ================ ManagedFunctionSourceInstanceContext ================
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
	public void setFunctionNamespaceTypeLoaded(boolean isFunctionNamespaceTypeLoaded) {
		if (this.currentPropertiesPage != null) {
			this.currentPropertiesPage.setPageComplete(isFunctionNamespaceTypeLoaded);
		}
	}

}