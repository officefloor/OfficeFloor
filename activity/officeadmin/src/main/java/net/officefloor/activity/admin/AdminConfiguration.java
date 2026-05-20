package net.officefloor.activity.admin;

import lombok.Data;
import net.officefloor.activity.compose.ComposeConfiguration;

@Data
public class AdminConfiguration extends ComposeConfiguration {

    private AdministrationConfiguration administration;
}
