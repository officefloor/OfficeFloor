package net.officefloor.activity.managedobject;

import lombok.Data;

import java.util.Map;

/**
 * Configuration for a {@link net.officefloor.compile.spi.office.OfficeSupplier}.
 */
@Data
public class SupplierSourceConfiguration {

    private String source;

    private Map<String, String> properties;

}
