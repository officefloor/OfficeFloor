package net.officefloor.plugin.clazz.dependency.impl;

import net.officefloor.plugin.clazz.dependency.ClassDependencyFactory;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturer;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturerContext;

/**
 * {@link ClassDependencyManufacturer} for providing a dependency
 * {@link Object}.
 * 
 * @author Daniel Sagenschneider
 */
public class ObjectClassDependencyManufacturer implements ClassDependencyManufacturer {

	/*
	 * ======================== ClassDependencyManufacturer =======================
	 */

	@Override
	public ClassDependencyFactory createParameterFactory(ClassDependencyManufacturerContext context) throws Exception {

		// Obtain the details of the dependency
		String qualifier = context.getDependencyQualifier();
		Class<?> requiredType = context.getDependencyClass();

		// Add the dependency
		int dependencyIndex = context.newDependency(requiredType).setQualifier(qualifier).build().getIndex();

		// Create and return the factory
		return new ObjectClassDependencyFactory(dependencyIndex);
	}

}