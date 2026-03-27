package net.officefloor.web.rest.build;

/**
 * Listens on the configuration of the {@link RestEndpoint}s.
 */
public interface RestEndpointListener {

    /**
     * Initialising for compiling in the {@link RestEndpoint}.
     *
     * @param context {@link RestEndpointContext}.
     */
    default void initialise(RestEndpointContext context) {
        // Do nothing
    }

    /**
     * Invoked for each {@link RestEndpoint} configured.
     *
     * @param endpoint {@link RestEndpoint}.
     */
    default void endpoint(RestEndpoint endpoint) {
        // Do nothing
    }

}
