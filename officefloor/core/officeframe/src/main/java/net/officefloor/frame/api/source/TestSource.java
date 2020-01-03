package net.officefloor.frame.api.source;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.officefloor.frame.api.OfficeFrame;

/**
 * <p>
 * Annotation to be applied to test/mock sources so they are ignored from being
 * dynamically &quot;discovered&quot; for use in configuration.
 * <p>
 * Typically source implementations will always be focused for actual deployed
 * use, however there are many source implementations in the tests for
 * {@link OfficeFrame} that should not be used other than for testing.
 * 
 * @author Daniel Sagenschneider
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TestSource {
}