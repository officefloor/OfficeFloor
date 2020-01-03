package net.officefloor.plugin.section.clazz;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * Annotates a {@link Field} for the {@link ClassSectionSource} for
 * configuration of a {@link SectionManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ManagedObject {

	/**
	 * Obtains the implementing {@link ManagedObjectSource}.
	 * 
	 * @return Implementing {@link ManagedObjectSource}.
	 */
	Class<? extends ManagedObjectSource<?, ?>> source();

	/**
	 * Obtains the {@link Property} instances.
	 * 
	 * @return {@link Property} instances.
	 */
	Property[] properties() default {};

	/**
	 * Obtains the {@link TypeQualifier} instances.
	 * 
	 * @return {@link TypeQualifier} instances.
	 */
	TypeQualifier[] qualifiers() default {};

	/**
	 * Obtains the {@link FlowLink} instances.
	 * 
	 * @return {@link FlowLink} instances.
	 */
	FlowLink[] flows() default {};

}