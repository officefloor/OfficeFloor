/*-
 * #%L
 * Web configuration
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.woof.objects;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.internal.structure.AutoWire;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.compile.spi.managedobject.ManagedObjectDependency;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeManagedObjectPool;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSupplier;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.section.clazz.ManagedObject;
import net.officefloor.woof.model.objects.PropertyFileModel;
import net.officefloor.woof.model.objects.PropertyModel;
import net.officefloor.woof.model.objects.PropertySourceModel;
import net.officefloor.woof.model.objects.TypeQualificationModel;
import net.officefloor.woof.model.objects.WoofDependencyModel;
import net.officefloor.woof.model.objects.WoofFlowModel;
import net.officefloor.woof.model.objects.WoofManagedObjectModel;
import net.officefloor.woof.model.objects.WoofObjectSourceModel;
import net.officefloor.woof.model.objects.WoofObjectsModel;
import net.officefloor.woof.model.objects.WoofObjectsRepository;
import net.officefloor.woof.model.objects.WoofPoolModel;
import net.officefloor.woof.model.objects.WoofStartAfterModel;
import net.officefloor.woof.model.objects.WoofStartBeforeModel;
import net.officefloor.woof.model.objects.WoofSupplierModel;

/**
 * {@link WoofObjectsLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofObjectsLoaderImpl implements WoofObjectsLoader {

	/**
	 * {@link WoofObjectsRepository}.
	 */
	private final WoofObjectsRepository repository;

	/**
	 * Initiate.
	 * 
	 * @param repository {@link WoofObjectsRepository}.
	 */
	public WoofObjectsLoaderImpl(WoofObjectsRepository repository) {
		this.repository = repository;
	}

	/*
	 * ======================= WoofObjectsLoader ===========================
	 */

	@Override
	public void loadWoofObjectsConfiguration(WoofObjectsLoaderContext context) throws Exception {

		// Obtain the details
		ConfigurationItem objectsConfiguration = context.getConfiguration();

		// Obtain the Office Architect and context
		OfficeArchitect architect = context.getOfficeArchitect();
		OfficeExtensionContext extensionContext = context.getOfficeExtensionContext();

		// Load the objects model
		WoofObjectsModel objects = new WoofObjectsModel();
		this.repository.retrieveWoofObjects(objects, objectsConfiguration);

		// Configure the objects
		for (WoofObjectSourceModel objectSource : objects.getWoofObjectSources()) {

			// Load based on object source type
			if (objectSource instanceof WoofManagedObjectModel) {
				// Load the managed object
				this.loadWoofManagedObject((WoofManagedObjectModel) objectSource, architect, extensionContext);

			} else if (objectSource instanceof WoofSupplierModel) {
				// Load the supplier
				this.loadWoofSupplier((WoofSupplierModel) objectSource, architect, extensionContext);

			} else {
				// Unknown object source
				throw new IllegalStateException(
						"Unknown object source configuration type " + objectSource.getClass().getName());
			}
		}
	}

	/**
	 * Loads the {@link WoofManagedObjectModel}.
	 * 
	 * @param managedObject {@link WoofManagedObjectModel}.
	 * @param architect     {@link OfficeArchitect}.
	 * @param context       {@link OfficeExtensionContext}.
	 * @throws Exception If fails to load {@link ManagedObject}.
	 */
	private void loadWoofManagedObject(final WoofManagedObjectModel managedObject, OfficeArchitect architect,
			OfficeExtensionContext context) throws Exception {

		// Obtain the managed object source
		String managedObjectSourceClassName = managedObject.getManagedObjectSourceClassName();
		String classManagedObjectSourceClass = null;
		if (managedObjectSourceClassName == null) {
			// No managed object source, so try for class
			classManagedObjectSourceClass = managedObject.getClassManagedObjectSourceClass();
			if (classManagedObjectSourceClass != null) {
				// Have class, so use class managed object
				managedObjectSourceClassName = ClassManagedObjectSource.class.getName();
			}
		}

		// Obtain the type qualifications
		List<AutoWire> typeQualifications = new LinkedList<AutoWire>();
		String qualifier = managedObject.getQualifier();
		String type = managedObject.getType();
		if (!CompileUtil.isBlank(type)) {
			// Shortcut type qualification provided
			typeQualifications.add(new AutoWire(qualifier, type));
		} else if ((!CompileUtil.isBlank(qualifier)) && (!CompileUtil.isBlank(classManagedObjectSourceClass))) {
			// No type, so default type from class for qualification
			typeQualifications.add(new AutoWire(qualifier, classManagedObjectSourceClass));
		}
		for (TypeQualificationModel autoWire : managedObject.getTypeQualifications()) {
			typeQualifications.add(new AutoWire(autoWire.getQualifier(), autoWire.getType()));
		}

		// Obtain the managed object name
		String managedObjectName = (typeQualifications.size() > 0 ? typeQualifications.get(0).toString()
				: !CompileUtil.isBlank(classManagedObjectSourceClass) ? classManagedObjectSourceClass
						: managedObjectSourceClassName);

		// Obtain the managed object scope
		String managedObjectScopeName = managedObject.getScope();
		final ManagedObjectScope managedObjectScope;
		try {
			managedObjectScope = CompileUtil.isBlank(managedObjectScopeName) ? ManagedObjectScope.THREAD
					: ManagedObjectScope.valueOf(managedObjectScopeName.toUpperCase());
		} catch (IllegalArgumentException ex) {
			// Invalid scope
			architect.addIssue("Invalid managed object scope '" + managedObjectScopeName + "' for managed object "
					+ managedObjectName);
			return; // invalid managed object so do not load
		}

		// Add the managed object source
		OfficeManagedObjectSource mos = architect.addOfficeManagedObjectSource(managedObjectName,
				managedObjectSourceClassName);

		// Load the properties
		if (classManagedObjectSourceClass != null) {
			// Class managed object source class name property always first
			mos.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, classManagedObjectSourceClass);
		}
		this.loadProperties(mos, managedObject.getPropertySources(), context);

		// Provide timeout (if provided)
		long timeout = managedObject.getTimeout();
		if (timeout > 0) {
			mos.setTimeout(timeout);
		}

		// Configure possible pooling
		WoofPoolModel poolModel = managedObject.getPool();
		if (poolModel != null) {
			OfficeManagedObjectPool pool = architect.addManagedObjectPool(managedObjectName + "_pool",
					poolModel.getManagedObjectPoolSourceClassName());
			this.loadProperties(pool, poolModel.getPropertySources(), context);
			architect.link(mos, pool);
		}

		// Configure the flows
		for (WoofFlowModel flow : managedObject.getFlows()) {
			architect.link(mos.getOfficeManagedObjectFlow(flow.getName()),
					architect.getOfficeSection(flow.getSection()).getOfficeSectionInput(flow.getInput()));
		}

		// Add the managed object
		OfficeManagedObject mo = mos.addOfficeManagedObject(managedObjectName, managedObjectScope);

		// Configure the type qualifiers
		for (AutoWire autoWire : typeQualifications) {
			mo.addTypeQualification(autoWire.getQualifier(), autoWire.getType());
		}

		// Configure the dependencies
		for (WoofDependencyModel dependencyModel : managedObject.getDependencies()) {
			ManagedObjectDependency dependency = mo.getOfficeManagedObjectDependency(dependencyModel.getName());
			dependency.setOverrideQualifier(dependencyModel.getQualifier());
			String specificType = dependencyModel.getType();
			if (!CompileUtil.isBlank(specificType)) {
				dependency.setSpecificType(dependencyModel.getType());
			}
		}

		// Configure the start befores
		for (WoofStartBeforeModel startBefore : managedObject.getStartBefores()) {
			architect.startBefore(mos, startBefore.getManagedObjectType());
		}

		// Configure the start afters
		for (WoofStartAfterModel startAfter : managedObject.getStartAfters()) {
			architect.startAfter(mos, startAfter.getManagedObjectType());
		}
	}

	/**
	 * Loads the {@link WoofSupplierModel}.
	 * 
	 * @param supplierModel {@link WoofSupplierModel}.
	 * @param architect     {@link OfficeArchitect}.
	 * @param context       {@link OfficeExtensionContext}.
	 * @throws IOException If failure loading {@link Property}.
	 */
	private void loadWoofSupplier(WoofSupplierModel supplierModel, OfficeArchitect architect,
			OfficeExtensionContext context) throws IOException {

		// Obtain the supplier details
		String supplierSourceClassName = supplierModel.getSupplierSourceClassName();

		// Add the supplier
		OfficeSupplier supplier = architect.addSupplier(supplierSourceClassName, supplierSourceClassName);

		// Load the properties
		this.loadProperties(supplier, supplierModel.getPropertySources(), context);
	}

	/**
	 * Loads the properties.
	 * 
	 * @param configurable    {@link PropertyConfigurable}.
	 * @param propertySources {@link PropertySourceModel} instances.
	 * @param context         {@link OfficeExtensionContext}.
	 * @throws IOException If fails to load the properties.
	 */
	private void loadProperties(PropertyConfigurable configurable, List<PropertySourceModel> propertySources,
			OfficeExtensionContext context) throws IOException {
		for (PropertySourceModel propertySource : propertySources) {

			// Load based on property source type
			if (propertySource instanceof PropertyModel) {
				// Load the property
				PropertyModel property = (PropertyModel) propertySource;
				configurable.addProperty(property.getName(), property.getValue());

			} else if (propertySource instanceof PropertyFileModel) {
				// Load properties from file
				PropertyFileModel propertyFile = (PropertyFileModel) propertySource;
				InputStream propertyConfiguration = context.getResource(propertyFile.getPath());
				Properties properties = new Properties();
				properties.load(propertyConfiguration);
				for (String propertyName : properties.stringPropertyNames()) {
					String propertyValue = properties.getProperty(propertyName);
					configurable.addProperty(propertyName, propertyValue);
				}

			} else {
				// Unknown property source
				throw new IllegalStateException("Unknown property source type " + propertySource.getClass().getName());
			}
		}
	}

}
