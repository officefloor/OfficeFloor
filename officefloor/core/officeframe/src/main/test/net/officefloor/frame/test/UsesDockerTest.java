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
 * {@link Test} that uses docker.
 * 
 * @author Daniel Sagenschneider
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Tag("docker")
@DisabledIfSystemProperty(named = SkipUtil.DOCKER_AVAILABLE_SYSTEM_PROPERTY, matches = "false")
@DisabledIfEnvironmentVariable(named = SkipUtil.DOCKER_AVAILABLE_ENVIRONMENT_VARIABLE, matches = "false")
@Test
public @interface UsesDockerTest {
}