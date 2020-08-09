package net.officefloor.frame.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Stress {@link Test}.
 * 
 * @author Daniel Sagenschneider
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Tag("stress")
@DisabledIfSystemProperty(named = "skipStress", matches = ".*")
@DisabledIfSystemProperty(named = "officefloor.skip.stress.tests", matches = "true")
@DisabledIfEnvironmentVariable(named = "OFFICEFLOOR_SKIP_STRESS_TESTS", matches = "true")
@Test
public @interface StressTest {
}