package net.officefloor.cabinet;

import net.officefloor.cabinet.spi.Index;
import net.officefloor.cabinet.spi.OfficeCabinet;

/**
 * Annotation for {@link OfficeCabinet} configuration on tests.
 * 
 * @author Daniel Sagenschneider
 */
public @interface MCabinet {

	/**
	 * {@link Document} type for {@link OfficeCabinet}.
	 * 
	 * @return {@link Document} type for {@link OfficeCabinet}.
	 */
	Class<?> value();

	/**
	 * {@link Index} instances for the {@link OfficeCabinet}.
	 * 
	 * @return {@link Index} instances for the {@link OfficeCabinet}.
	 */
	MIndex[] indexes() default {};

}