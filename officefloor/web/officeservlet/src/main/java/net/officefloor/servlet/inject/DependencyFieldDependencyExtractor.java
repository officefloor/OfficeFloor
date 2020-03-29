package net.officefloor.servlet.inject;

import java.lang.reflect.Field;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.managedobject.clazz.Dependency;

/**
 * {@link Dependency} {@link FieldDependencyExtractor}.
 * 
 * @author Daniel Sagenschneider
 */
public class DependencyFieldDependencyExtractor
		implements FieldDependencyExtractor, FieldDependencyExtractorServiceFactory {

	/*
	 * =================== FieldDependencyExtractorServiceFactory =================
	 */

	@Override
	public FieldDependencyExtractor createService(ServiceContext context) throws Throwable {
		return this;
	}

	@Override
	public RequiredDependency extractRequiredDependency(Field field) {
		return field.isAnnotationPresent(Dependency.class) ? new RequiredDependency(null, field.getType()) : null;
	}

}