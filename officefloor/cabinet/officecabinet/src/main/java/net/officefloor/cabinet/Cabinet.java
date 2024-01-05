package net.officefloor.cabinet;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import net.officefloor.cabinet.domain.DomainCabinetFactory;

/**
 * Annotates an <code>interface</code> to be provided by
 * {@link DomainCabinetFactory} for dependency injection.
 * 
 * @author Daniel Sagenschneider
 */
@Documented
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface Cabinet {
}