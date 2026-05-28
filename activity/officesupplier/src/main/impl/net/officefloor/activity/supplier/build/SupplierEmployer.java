package net.officefloor.activity.supplier.build;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ScanResult;
import net.officefloor.activity.supplier.SupplierConfiguration;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSupplier;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;

import java.util.HashMap;
import java.util.Map;

public class SupplierEmployer {

    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

    /**
     * Employs the {@link SupplierArchitect}.
     *
     * @param officeArchitect {@link OfficeArchitect}.
     * @param context         {@link OfficeSourceContext}.
     * @return {@link SupplierArchitect}.
     */
    public static SupplierArchitect employSupplierArchitect(OfficeArchitect officeArchitect,
                                                            OfficeSourceContext context) {
        return new SupplierArchitect() {

            @Override
            public OfficeSupplier addSupplier(String supplierName, String supplierLocation,
                                              PropertyList properties) throws Exception {
                SupplierConfiguration config = MAPPER.readValue(
                        context.getConfigurationItem(supplierLocation, properties).getReader(),
                        SupplierConfiguration.class);
                return createSupplier(supplierName, config, officeArchitect);
            }

            @Override
            public Map<String, OfficeSupplier> addSuppliers(String supplierDirectory,
                                                            PropertyList properties) throws Exception {
                Map<String, OfficeSupplier> suppliers = new HashMap<>();

                String dir = supplierDirectory;
                while (dir.endsWith("/")) {
                    dir = dir.substring(0, dir.length() - 1);
                }
                dir = dir + "/";

                try (ScanResult result = new ClassGraph().acceptPaths(dir).scan()) {
                    for (String yamlExtension : new String[]{"yml", "yaml"}) {
                        for (Resource resource : result.getResourcesWithExtension(yamlExtension)) {
                            String path = resource.getPath();
                            String supplierName = path.substring(dir.length(),
                                    path.length() - ".".length() - yamlExtension.length());
                            SupplierConfiguration config = MAPPER.readValue(
                                    context.getConfigurationItem(path, properties).getReader(),
                                    SupplierConfiguration.class);
                            suppliers.put(supplierName, createSupplier(supplierName, config, officeArchitect));
                        }
                    }
                }
                return suppliers;
            }
        };
    }

    private static OfficeSupplier createSupplier(String supplierName, SupplierConfiguration config,
                                                 OfficeArchitect officeArchitect) {
        OfficeSupplier supplier = officeArchitect.addSupplier(supplierName, config.getSource());
        Map<String, String> properties = config.getProperties();
        if (properties != null) {
            properties.forEach(supplier::addProperty);
        }
        return supplier;
    }

}
