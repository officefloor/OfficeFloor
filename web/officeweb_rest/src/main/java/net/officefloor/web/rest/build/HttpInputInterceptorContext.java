package net.officefloor.web.rest.build;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeFlowSinkNode;
import net.officefloor.compile.spi.office.OfficeFlowSourceNode;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.web.build.HttpInput;

/**
 * Context for the {@link HttpInputInterceptor}.
 */
public interface HttpInputInterceptorContext {

    /**
     * Adds intercepting of the {@link HttpInput} before handling.
     *
     * @param input  {@link OfficeFlowSinkNode} to invoke for interception.
     * @param output {@link OfficeFlowSourceNode} for output of interception to next.
     */
    void link(OfficeFlowSinkNode input, OfficeFlowSourceNode output);

    /**
     * Obtains the {@link OfficeArchitect} to establish links.
     *
     * @return {@link OfficeArchitect}.
     */
    OfficeArchitect getOfficeArchitect();

    /**
     * Obtains the {@link OfficeSourceContext}.
     *
     * @return {@link OfficeSourceContext}.
     */
    OfficeSourceContext getOfficeSourceContext();

}
