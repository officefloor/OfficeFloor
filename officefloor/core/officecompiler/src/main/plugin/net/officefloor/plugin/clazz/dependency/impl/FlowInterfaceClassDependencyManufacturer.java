package net.officefloor.plugin.clazz.dependency.impl;

import java.lang.annotation.Annotation;

import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturer;

/**
 * {@link ClassDependencyManufacturer} for providing a dependency
 * {@link FlowInterface}.
 * 
 * @author Daniel Sagenschneider
 */
public class FlowInterfaceClassDependencyManufacturer extends AbstractFlowClassDependencyManufacturer {

	@Override
	protected Class<? extends Annotation> getAnnotationType() {
		return FlowInterface.class;
	}

}