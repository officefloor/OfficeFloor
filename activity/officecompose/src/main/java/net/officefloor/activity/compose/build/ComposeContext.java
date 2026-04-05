package net.officefloor.activity.compose.build;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;

/**
 * Context for the {@link ComposeBuilder}.
 */
public interface ComposeContext<C> {

    C getConfiguration();

    OfficeArchitect getOfficeArchitect();

    OfficeSourceContext getOfficeSourceContext();

    OfficeSectionInput getStartFunction();

    OfficeSectionInput getFunction(String functionName);

}
