package net.officefloor.web.template;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.officefloor.web.template.parse.ParsedTemplate;

/**
 * Flags that the {@link ParsedTemplate} should not be rendered after the method
 * completes. This is to avoid the default behaviour of re-rendering the
 * {@link ParsedTemplate}.
 * 
 * @author Daniel Sagenschneider
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface NotRenderTemplateAfter {
}