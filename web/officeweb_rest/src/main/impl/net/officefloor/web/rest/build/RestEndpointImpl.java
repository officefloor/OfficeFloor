package net.officefloor.web.rest.build;

import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.web.build.HttpInput;

/**
 * {@link RestEndpoint} implementation.
 */
public class RestEndpointImpl implements RestEndpoint {

    private final boolean isSecure;

    private final HttpMethod method;

    private final String path;

    private final HttpInput httpInput;

    private OfficeSectionInput serviceInput;

    public RestEndpointImpl(boolean isSecure, HttpMethod method, String path, HttpInput httpInput, OfficeSectionInput serviceInput) {
        this.isSecure = isSecure;
        this.method = method;
        this.path = path;
        this.httpInput = httpInput;
        this.serviceInput = serviceInput;
    }

    /*
     * ================== RestEndpoint ===================
     */

    @Override
    public boolean isSecure() {
        return this.isSecure;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return this.method;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public HttpInput getHttpInput() {
        return this.httpInput;
    }

    @Override
    public OfficeSectionInput getServiceInput() {
        return this.serviceInput;
    }
}
