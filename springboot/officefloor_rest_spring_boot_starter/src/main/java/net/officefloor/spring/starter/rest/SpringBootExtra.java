package net.officefloor.spring.starter.rest;

import net.officefloor.frame.api.source.SourceContext;

/**
 * <p>
 * Provides means to load services from other Spring boot starters (other than Spring MVC).
 * <p>
 * This allows users to selectively use what they require.
 */
public class SpringBootExtra {

    /**
     * Loads the Service.
     *
     * @param extraClassName   {@link Class} that will exist if the Spring Boot extra is on the class path.
     * @param serviceClassName {@link Class} of service.
     * @param sourceContext    {@link SourceContext}.
     * @param defaultService   Default service. May be <code>null</code>.
     * @param <S>              Service type.
     * @return Service.
     * @throws Exception If fails to load service.
     */
    public static <S> S loadService(String extraClassName, String serviceClassName, SourceContext sourceContext, S defaultService) throws Exception {

        // Determine if the Spring Boot extra is on the class path
        Class<?> extraClass = sourceContext.loadOptionalClass(extraClassName);
        if (extraClass == null) {

            // Spring boot not included, so service unavailable (use default)
            return defaultService;
        }

        // Load the service
        Class<?> serviceClass = sourceContext.loadClass(serviceClassName);
        Object service = serviceClass.getConstructor().newInstance();
        return (S) service;
    }

}