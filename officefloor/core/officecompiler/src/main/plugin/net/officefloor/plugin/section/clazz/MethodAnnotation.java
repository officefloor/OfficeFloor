package net.officefloor.plugin.section.clazz;

import java.lang.reflect.Method;

/**
 * Annotates {@link net.officefloor.frame.api.function.ManagedFunction}
 * with the {@link Method} implementing it.
 */
public interface MethodAnnotation {

    /**
     * Obtains the {@link Method}.
     *
     * @return {@link Method}.
     */
    Method getMethod();
}
