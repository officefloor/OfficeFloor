package net.officefloor.activity.supplier;

import lombok.Data;

import java.util.Map;

/**
 * Configuration for an {@link net.officefloor.compile.spi.office.OfficeSupplier}.
 */
@Data
public class SupplierConfiguration {

    private String source;

    private Map<String, String> properties;

}
