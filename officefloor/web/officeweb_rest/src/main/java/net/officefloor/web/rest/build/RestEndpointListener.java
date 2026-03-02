package net.officefloor.web.rest.build;

/**
 * Listens on the configuration of the REST endpoints.
 */
public interface RestEndpointListener {

    /**
     * Initialising for compiling in the REST endpoint.
     *
     * @param context {@link RestEndpointContext}.
     */
    void initialise(RestEndpointContext context);

    /**
     * Invoked for each REST endpoint configured.
     *
     * @param endpoint {@link RestEndpoint}.
     */
    void endpoint(RestEndpoint endpoint);
}
