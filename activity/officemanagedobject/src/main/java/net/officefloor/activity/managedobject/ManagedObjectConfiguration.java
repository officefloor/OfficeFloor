package net.officefloor.activity.managedobject;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import net.officefloor.activity.compose.ComposeConfiguration;

@Data
public class ManagedObjectConfiguration extends ComposeConfiguration {

    @JsonProperty("managed-object")
    ManagedObjectSourceConfiguration managedObject;

}
