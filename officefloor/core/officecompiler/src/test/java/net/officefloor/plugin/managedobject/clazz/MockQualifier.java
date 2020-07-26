package net.officefloor.plugin.managedobject.clazz;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import net.officefloor.plugin.clazz.Qualifier;

/**
 * Mock {@link Qualifier}.
 * 
 * @author Daniel Sagenschneider
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface MockQualifier {
}