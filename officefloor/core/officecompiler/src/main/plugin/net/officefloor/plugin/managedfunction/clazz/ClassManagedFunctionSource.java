package net.officefloor.plugin.managedfunction.clazz;

import java.lang.reflect.Method;

import net.officefloor.compile.ManagedFunctionSourceService;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.plugin.managedfunction.method.AbstractFunctionManagedFunctionSource;

/**
 * {@link ManagedFunctionSource} for a {@link Class} having the {@link Method}
 * instances as the {@link ManagedFunction} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassManagedFunctionSource extends AbstractFunctionManagedFunctionSource
		implements ManagedFunctionSourceService<ClassManagedFunctionSource> {

	/*
	 * =================== ManagedFunctionSourceService ===================
	 */

	@Override
	public String getManagedFunctionSourceAlias() {
		return "CLASS";
	}

	@Override
	public Class<ClassManagedFunctionSource> getManagedFunctionSourceClass() {
		return ClassManagedFunctionSource.class;
	}

}