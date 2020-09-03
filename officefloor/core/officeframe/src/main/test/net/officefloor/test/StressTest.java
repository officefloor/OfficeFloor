package net.officefloor.test;

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
@DisabledIfSystemProperty(named = SkipUtil.SKIP_STRESS_SYSTEM_PROPERTY, matches = "true")
@DisabledIfEnvironmentVariable(named = SkipUtil.SKIP_STRESS_ENVIRONMENT_VARIABLE, matches = "true")
@Test
public @interface StressTest {
}