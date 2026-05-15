package net.officefloor.web.rest.build;

/**
 * <p>
 * Overrides the default linking of the {@link net.officefloor.web.build.HttpInput} to its service
 * {@link net.officefloor.compile.spi.office.OfficeSectionInput}. Allows intercepting logic to be
 * inserted before the {@link RestMethod} handling is invoked.
 * <p>
 * This is typically useful for Security, CORS or other aspects that wrap {@link RestMethod} execution.
 */
@FunctionalInterface
public interface HttpInputInterceptor {

    /**
     * Intercepts the {@link net.officefloor.web.build.HttpInput} to the service section,
     * optionally inserting intermediate logic.
     *
     * @param context {@link HttpInputInterceptorContext}.
     */
    void intercept(HttpInputInterceptorContext context);

}
