package net.officefloor.web.rest.build;

/**
 * Listens on the configuration of the {@link RestEndpoint}s.
 */
@FunctionalInterface
public interface RestListener {

    /**
     * Initialising for compiling in the {@link RestEndpoint}.
     *
     * @param context {@link RestEndpointContext}.
     */
    default void initialiseRestEndpoint(RestEndpointContext context) {
        // Do nothing
    }

    /**
     * Initialising for comping the {@link RestMethod}.
     *
     * @param context {@link RestMethodContext}.
     */
    default void initialiseRestMethod(RestMethodContext context) {
        // Do nothing
    }

    /**
     * Invoked for each {@link RestEndpoint} configured.
     *
     * @param endpoint {@link RestEndpoint}.
     */
    void endpoint(RestEndpoint endpoint);

}
