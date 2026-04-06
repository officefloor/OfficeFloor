package net.officefloor.activity.govern;

import lombok.Data;
import net.officefloor.activity.compose.ComposeConfiguration;

/**
 * Configuration for {@link net.officefloor.compile.spi.office.OfficeGovernance} composition.
 */
@Data
public class GovernConfiguration extends ComposeConfiguration {
    private GovernanceConfiguration governance;
}
