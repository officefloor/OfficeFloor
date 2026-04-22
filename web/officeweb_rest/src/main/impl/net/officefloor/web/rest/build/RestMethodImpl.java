package net.officefloor.web.rest.build;

import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.web.build.HttpInput;

/**
 * {@link RestMethod} implementation.
 */
public class RestMethodImpl implements RestMethod {

    private final boolean isSecure;

    private final HttpMethod httpMethod;

    private final HttpInput httpInput;

    private final OfficeSectionInput sectionInput;

    private final RestConfiguration configuration;

    /**
     * Instantiate.
     *
     * @param isSecure      Indicates if {@link HttpMethod} requires secure connection.
     * @param httpMethod    {@link HttpMethod}.
     * @param httpInput     {@link HttpInput} to service this {@link RestMethod}.
     * @param sectionInput  {@link OfficeSectionInput} to service this {@link RestMethod}.
     * @param configuration Specific {@link RestConfiguration} for this {@link RestMethod}.
     */
    public RestMethodImpl(boolean isSecure, HttpMethod httpMethod, HttpInput httpInput,
                          OfficeSectionInput sectionInput, RestConfiguration configuration) {
        this.isSecure = isSecure;
        this.httpMethod = httpMethod;
        this.httpInput = httpInput;
        this.sectionInput = sectionInput;
        this.configuration = configuration;
    }

    /*
     * =================== RestMethod ===================
     */

    @Override
    public boolean isSecure() {
        return this.isSecure;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return this.httpMethod;
    }

    @Override
    public HttpInput getHttpInput() {
        return this.httpInput;
    }

    @Override
    public OfficeSectionInput getServiceInput() {
        return this.sectionInput;
    }

    @Override
    public <T> T getConfiguration(String itemName, Class<T> type) {
        return this.configuration.getConfiguration(itemName, type);
    }
}
