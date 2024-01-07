package net.officefloor.cabinet;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import net.officefloor.cabinet.spi.OfficeStore;

/**
 * {@link Annotation} for no {@link OfficeStore}.
 * 
 * @author Daniel Sagenschneider
 */
@Retention(RUNTIME)
@Target(ElementType.METHOD)
public @interface MStoreNone {
}
