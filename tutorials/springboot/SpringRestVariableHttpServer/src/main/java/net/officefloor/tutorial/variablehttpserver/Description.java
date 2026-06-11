package net.officefloor.tutorial.variablehttpserver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.officefloor.plugin.clazz.Qualifier;

/**
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Description {
}
// END SNIPPET: tutorial