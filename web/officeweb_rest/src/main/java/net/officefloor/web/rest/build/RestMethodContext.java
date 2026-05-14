package net.officefloor.web.rest.build;

import net.officefloor.server.http.HttpMethod;

/**
 * Context for the {@link RestMethod}.
 */
public interface RestMethodContext<M> {

    /**
     * Indicates if secure (e.g. HTTPS).
     *
     * @return <code>true</code> if secure.
     */
    boolean isSecure();

    /**
     * Allows overriding if secure.
     *
     * @param isSecure Specify if secure.
     */
    void setSecure(boolean isSecure);

    /**
     * Obtains the REST path for this {@link RestMethodContext}.
     *
     * @return REST path for this {@link RestMethodContext}.
     */
    RestPathContext getPath();

    /**
     * Obtains the {@link HttpMethod}.
     *
     * @return {@link HttpMethod}.
     */
    HttpMethod getHttpMethod();

    /**
     * <p>
     * Obtains additional configuration for the {@link RestMethod}.
     * <p>
     * This for example is CORS specific configuration for the {@link RestMethod}.
     *
     * @param itemName Name of configuration item.
     * @param type     Type of configuration.
     * @param <T>      Type of configuration.
     * @return Configuration item.
     */
    <T> T getConfiguration(String itemName, Class<T> type);

    /**
     * Overrides the default linking of the {@link net.officefloor.web.build.HttpInput} to the
     * service {@link net.officefloor.compile.spi.office.OfficeSectionInput}. When not set the
     * default direct link is established.
     *
     * @param linker {@link HttpInputInterceptor}.
     */
    void addHttpInputInterceptor(HttpInputInterceptor linker);

    /**
     * Optionally sets a Momento on the {@link RestMethod}.
     *
     * @param momento Momento on the {@link RestMethod} available via the {@link MomentoKey}.
     */
    void setMomento(M momento);

}
