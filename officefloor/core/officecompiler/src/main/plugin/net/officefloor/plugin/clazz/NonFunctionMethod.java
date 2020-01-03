package net.officefloor.plugin.clazz;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.plugin.managedfunction.clazz.ClassManagedFunctionSource;

/**
 * Enables flagging a <code>public</code> method of a {@link Class} to not be
 * {@link ManagedFunction} for the {@link ClassManagedFunctionSource}.
 * 
 * @author Daniel Sagenschneider
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface NonFunctionMethod {
}