package net.officefloor.web.rest.build;

import net.officefloor.server.http.HttpMethod;

/**
 * Context for the {@link RestMethod}.
 */
public interface RestMethodContext extends RestEndpointContext {

    /**
     * Obtains the {@link HttpMethod}.
     *
     * @return {@link HttpMethod}.
     */
    HttpMethod getHttpMethod();

    /**
     * Overrides the default linking of the {@link net.officefloor.web.build.HttpInput} to the
     * service {@link net.officefloor.compile.spi.office.OfficeSectionInput}. When not set the
     * default direct link is established.
     *
     * @param linker {@link HttpInputLinker}.
     */
    void setHttpInputLinker(HttpInputLinker linker);

}
