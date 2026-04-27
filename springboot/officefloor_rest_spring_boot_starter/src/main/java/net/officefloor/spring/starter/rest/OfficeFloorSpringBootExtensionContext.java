package net.officefloor.spring.starter.rest;

import net.officefloor.activity.compose.build.ComposeArchitect;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.web.rest.build.RestArchitect;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Context for the {@link OfficeFloorSpringBootExtension}.
 */
public interface OfficeFloorSpringBootExtensionContext {

    /**
     * Obtains the {@link OfficeArchitect}.
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

    /**
     * Obtains the {@link ComposeArchitect}.
     *
     * @return {@link ComposeArchitect}.
     */
    ComposeArchitect getComposeArchitect();

    /**
     * Obtains the {@link RestArchitect}.
     *
     * @return {@link RestArchitect}.
     */
    RestArchitect getRestArchitect();

    /**
     * Obtains the {@link ConfigurableApplicationContext}.
     *
     * @return {@link ConfigurableApplicationContext}.
     */
    ConfigurableApplicationContext getApplicationContext();

}
