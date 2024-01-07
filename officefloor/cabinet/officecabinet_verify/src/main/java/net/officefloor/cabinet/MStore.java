package net.officefloor.cabinet;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import net.officefloor.cabinet.spi.OfficeCabinet;
import net.officefloor.cabinet.spi.OfficeStore;

/**
 * {@link Annotation} for the {@link OfficeStore} configuration on tests.
 * 
 * @author Daniel Sagenschneider
 */
@Retention(RUNTIME)
@Target(ElementType.METHOD)
public @interface MStore {

	/**
	 * Domain {@link OfficeCabinet} type.
	 * 
	 * @return Domain {@link OfficeCabinet} type.
	 */
	Class<?> cabinetDomainType() default Object.class;

	/**
	 * {@link OfficeCabinet} configurations for the {@link OfficeStore}.
	 * 
	 * @return {@link OfficeCabinet} configurations for the {@link OfficeStore}.
	 */
	MCabinet[] cabinets() default {};

}
