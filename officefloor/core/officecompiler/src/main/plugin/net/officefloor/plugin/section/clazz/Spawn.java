package net.officefloor.plugin.section.clazz;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.plugin.clazz.FlowInterface;

/**
 * Annotates a {@link Method} of the {@link FlowInterface} to indicate to spawn
 * a {@link ThreadState}.
 * 
 * @author Daniel Sagenschneider
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Spawn {
}