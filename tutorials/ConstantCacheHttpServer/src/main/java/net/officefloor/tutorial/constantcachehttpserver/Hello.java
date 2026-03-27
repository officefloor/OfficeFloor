package net.officefloor.tutorial.constantcachehttpserver;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import net.officefloor.cache.Cache;
import net.officefloor.plugin.clazz.Qualifier;

/**
 * Identifies the Hello {@link Cache}.
 * 
 * @author Daniel Sagenschneider
 */
@Documented
@Retention(RUNTIME)
@Target(PARAMETER)
@Qualifier
public @interface Hello {
}
