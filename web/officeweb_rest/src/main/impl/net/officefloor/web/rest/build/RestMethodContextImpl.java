package net.officefloor.web.rest.build;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.web.build.HttpInput;
import net.officefloor.web.build.WebArchitect;

/**
 * {@link RestMethodContext} implementation.
 */
public class RestMethodContextImpl implements RestMethodContext {

    private boolean isSecure;

    private final HttpMethod httpMethod;

    private final String path;

    private final OfficeSectionInput sectionInput;

    private final RestConfiguration configuration;

    public RestMethodContextImpl(boolean isSecure, HttpMethod httpMethod,
                                 String path, OfficeSectionInput sectionInput,
                                 RestConfiguration configuration) {
        this.isSecure = isSecure;
        this.httpMethod = httpMethod;
        this.path = path;
        this.sectionInput = sectionInput;
        this.configuration = configuration;
    }

    public RestMethod buildRestMethod(WebArchitect webArchitect, OfficeArchitect officeArchitect) {

        // Obtain the REST input
        HttpInput httpInput = webArchitect.getHttpInput(this.isSecure, this.httpMethod.getName(), this.path);

        // Handle REST request
        officeArchitect.link(httpInput.getInput(), this.sectionInput);

        // Create and return rest method
        return new RestMethodImpl(this.isSecure, this.httpMethod, httpInput, this.sectionInput, this.configuration);
    }

    /*
     * ==================== RestMethodContext =================
     */

    @Override
    public HttpMethod getHttpMethod() {
        return this.httpMethod;
    }

    @Override
    public boolean isSecure() {
        return this.isSecure;
    }

    @Override
    public void setSecure(boolean isSecure) {
        this.isSecure = isSecure;
    }

    @Override
    public String getPath() {
        return this.path;
    }
}
