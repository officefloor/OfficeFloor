package net.officefloor.web.rest.build;

import net.officefloor.activity.compose.build.ComposeArchitect;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.web.build.WebArchitect;

/**
 * Context for the {@link RestMethod}.
 */
public interface RestMethodDecoratorContext<M> {

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
     * Obtains the REST path for this {@link RestMethodDecoratorContext}.
     *
     * @return REST path for this {@link RestMethodDecoratorContext}.
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
     * Allows intercepting functionality before the servicing
     * {@link net.officefloor.compile.spi.office.OfficeSectionInput} is invoked.
     *
     * @param interceptor {@link HttpInputInterceptor}.
     */
    void addHttpInputInterceptor(HttpInputInterceptor interceptor);

    /**
     * Optionally sets a Momento on the {@link RestMethod}.
     *
     * @param momento Momento on the {@link RestMethod} available via the {@link MomentoKey}.
     */
    void setMomento(M momento);

    /**
     * Obtains the {@link OfficeArchitect}.
     *
     * @return {@link OfficeArchitect}.
     */
    OfficeArchitect getOfficeArchitect();

    /**
     * Obtains the {@link WebArchitect}.
     *
     * @return {@link WebArchitect}.
     */
    WebArchitect getWebArchitect();

    /**
     * Obtains the {@link ComposeArchitect}.
     *
     * @return {@link ComposeArchitect}.
     */
    ComposeArchitect getComposeArchitect();

    /**
     * Obtains the {@link OfficeSourceContext}.
     *
     * @return {@link OfficeSourceContext}.
     */
    OfficeSourceContext getOfficeSourceContext();

}