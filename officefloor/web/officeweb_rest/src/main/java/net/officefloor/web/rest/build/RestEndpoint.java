package net.officefloor.web.rest.build;

import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.web.build.HttpInput;

/**
 * Configured REST endpoint.
 */
public interface RestEndpoint {

    /**
     * Indicates if the {@link RestEndpoint} is secure (e.g. HTTPS).
     *
     * @return <code>true</code> if {@link RestEndpoint} is secure.
     */
    boolean isSecure();

    /**
     * Obtains the {@link HttpMethod} for the {@link RestEndpoint}.
     *
     * @return {@link HttpMethod} for the {@link RestEndpoint}.
     */
    HttpMethod getHttpMethod();

    /**
     * Obtains the path for the {@link RestEndpoint}.
     *
     * @return Path for the {@link RestEndpoint}.
     */
    String getPath();

    /**
     * Obtains the {@link HttpInput} for the {@link RestEndpoint}.
     *
     * @return {@link HttpInput} for the {@link RestEndpoint}.
     */
    HttpInput getHttpInput();

    /**
     * Obtains the {@link OfficeSectionInput} to service the {@link RestEndpoint}.
     *
     * @return {@link OfficeSectionInput} to service the {@link RestEndpoint}.
     */
    OfficeSectionInput getServiceInput();

}
