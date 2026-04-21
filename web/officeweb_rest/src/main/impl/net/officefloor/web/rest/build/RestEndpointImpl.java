package net.officefloor.web.rest.build;

import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.web.build.HttpInput;

import java.util.LinkedList;
import java.util.List;

/**
 * {@link RestEndpoint} implementation.
 */
public class RestEndpointImpl implements RestEndpoint {

    private final String path;

    private final RestConfiguration configuration;

    private final List<RestMethod> methods = new LinkedList<>();

    /**
     * Instantiate the {@link RestEndpoint}.
     *
     * @param path          Path for the {@link RestEndpoint}.
     * @param configuration Generic {@link RestConfiguration} for this
     *                      {@link RestEndpoint} that can apply to all {@link RestMethod} instances.
     */
    public RestEndpointImpl(String path, RestConfiguration configuration) {
        this.path = path;
        this.configuration = configuration;
    }

    /**
     * Adds a {@link RestMethod}.
     *
     * @param restMethod {@link RestMethod}.
     */
    protected void addRestMethod(RestMethod restMethod) {
        this.methods.add(restMethod);
    }

    /*
     * ================== RestEndpoint ===================
     */

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public List<RestMethod> getRestMethods() {
        return null;
    }

    @Override
    public <T> T getConfiguration(String itemName, Class<T> type) {
        return this.configuration.getConfiguration(itemName, type);
    }
}
