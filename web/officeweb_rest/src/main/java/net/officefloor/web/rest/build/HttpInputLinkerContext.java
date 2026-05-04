package net.officefloor.web.rest.build;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.web.build.HttpInput;

/**
 * Context for the {@link HttpInputLinker}.
 */
public interface HttpInputLinkerContext {

    /**
     * Obtains the {@link HttpInput}.
     *
     * @return {@link HttpInput}.
     */
    HttpInput getHttpInput();

    /**
     * Obtains the {@link OfficeSectionInput} for the {@link RestMethod} service.
     *
     * @return {@link OfficeSectionInput} for the {@link RestMethod} service.
     */
    OfficeSectionInput getServiceInput();

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
