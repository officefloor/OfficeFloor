package net.officefloor.cloud.test;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.TestTemplate;

/**
 * Alias to {@link TestTemplate} to be clearer on purpose of test.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@TestTemplate
public @interface CloudTest {
}