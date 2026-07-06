package net.officefloor.tutorial.constantcachehttpserver;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import net.officefloor.plugin.clazz.Qualifier;

@Documented
@Retention(RUNTIME)
@Target(PARAMETER)
@Qualifier
public @interface World {
}
