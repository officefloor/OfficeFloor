package net.officefloor.cabinet;

import java.lang.annotation.Annotation;

import net.officefloor.cabinet.spi.Index;

/**
 * {@link Annotation} for {@link Index} configuration on tests.
 * 
 * @author Daniel Sagenschneider
 */
public @interface MIndex {

	/**
	 * Sort field name.
	 * 
	 * @return Sort field name.
	 */
	String sort() default "";

	/**
	 * Index field names.
	 * 
	 * @return Index field names.
	 */
	String[] value();

}
