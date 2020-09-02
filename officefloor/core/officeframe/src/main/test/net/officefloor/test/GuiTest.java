package net.officefloor.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * GUI {@link Test}.
 * 
 * @author Daniel Sagenschneider
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Tag("gui")
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
@Test
public @interface GuiTest {
}
