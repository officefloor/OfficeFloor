package net.officefloor.plugin.managedobject.clazz.injection;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.managedobject.clazz.ClassDependencyFactory;
import net.officefloor.plugin.managedobject.clazz.ClassDependencyManufacturer;
import net.officefloor.plugin.managedobject.clazz.ClassDependencyManufacturerContext;
import net.officefloor.plugin.managedobject.clazz.ClassDependencyManufacturerServiceFactory;

/**
 * {@link ClassDependencyManufacturer} for providing a dependency
 * {@link Object}.
 * 
 * @author Daniel Sagenschneider
 */
public class ObjectClassDependencyManufacturer
		implements ClassDependencyManufacturer, ClassDependencyManufacturerServiceFactory {

	/*
	 * ================= ClassDependencyManufacturerServiceFactory ================
	 */

	@Override
	public ClassDependencyManufacturer createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ======================== ClassDependencyManufacturer =======================
	 */

	@Override
	public ClassDependencyFactory createParameterFactory(ClassDependencyManufacturerContext context) throws Exception {

		// Obtain the details of the dependency
		String qualifier = context.getDependencyQualifier();
		Class<?> requiredType = context.getDependencyClass();

		// Add the dependency
		int dependencyIndex = context.addDependency(requiredType).setTypeQualifier(qualifier).getIndex();

		// Create and return the factory
		return new ObjectClassDependencyFactory(dependencyIndex);
	}

}