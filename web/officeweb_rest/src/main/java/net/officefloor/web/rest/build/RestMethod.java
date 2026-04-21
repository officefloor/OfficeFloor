package net.officefloor.web.rest.build;

import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.web.build.HttpInput;

/**
 * REST supported {@link HttpMethod}.
 */
public interface RestMethod {

    /**
     * Indicates if the {@link RestMethod} is secure (e.g. HTTPS).
     *
     * @return <code>true</code> if {@link RestMethod} is secure.
     */
    boolean isSecure();

    /**
     * Obtains the {@link HttpMethod}.
     *
     * @return {@link HttpMethod}.
     */
    HttpMethod getHttpMethod();

    /**
     * Obtains the {@link HttpInput}.
     *
     * @return {@link HttpInput}.
     */
    HttpInput getHttpInput();

    /**
     * Obtains the {@link OfficeSectionInput} to service the {@link RestMethod}.
     *
     * @return {@link OfficeSectionInput} to service the {@link RestMethod}.
     */
    OfficeSectionInput getServiceInput();

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

}