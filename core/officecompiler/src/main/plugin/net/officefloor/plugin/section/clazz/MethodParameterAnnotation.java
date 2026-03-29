package net.officefloor.plugin.section.clazz;

import java.lang.reflect.Method;

/**
 * Annotates the {@link net.officefloor.frame.api.function.ManagedFunction} object with
 * the details of the parameter for the {@link MethodAnnotation}.
 */
public interface MethodParameterAnnotation {

    /**
     * Obtains the {@link Method}.
     *
     * @return {@link Method}.
     */
    Method getMethod();

    /**
     * Obtains the index of the parameter on the {@link java.lang.reflect.Method}.
     *
     * @return Index of the parameter on the {@link java.lang.reflect.Method}.
     */
    int getParameterIndex();
}
