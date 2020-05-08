package net.officefloor.servlet.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Inject;
import javax.inject.Qualifier;

/**
 * Qualified {@link Inject}.
 * 
 * @author Daniel Sagenschneider
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface QualifiedInject {
}