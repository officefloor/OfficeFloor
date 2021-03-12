package net.officefloor.nosql.cosmosdb;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates an entity to specify details of the entity.
 * 
 * @author Daniel Sagenschneider
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface CosmosEntity {

	/**
	 * <p>
	 * Specifies the container identifier for the entity.
	 * <p>
	 * Leaving blank will use {@link Class} simple name.
	 * 
	 * @return Container identifier for the entity.
	 */
	String containerId() default "";

}