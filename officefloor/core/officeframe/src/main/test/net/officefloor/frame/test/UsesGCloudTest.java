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
 * {@link Test} that uses GCloud.
 * 
 * @author Daniel Sagenschneider
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Tag("gcloud")
@DisabledIfSystemProperty(named = "officefloor.gcloud.available", matches = "false")
@DisabledIfEnvironmentVariable(named = "OFFICEFLOOR_GCLOUD_AVAILABLE", matches = "false")
@Test
public @interface UsesGCloudTest {
}