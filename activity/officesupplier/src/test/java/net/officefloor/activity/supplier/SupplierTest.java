package net.officefloor.activity.supplier;

import net.officefloor.activity.supplier.build.SupplierArchitect;
import net.officefloor.activity.supplier.build.SupplierEmployer;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SupplierTest {

    @Test
    public void simple() throws Throwable {
        SimpleService.supplied = null;
        this.doTest(SimpleService.class, (architect, properties) ->
                architect.addSupplier("first", "officefloor/suppliers/first.yml", properties));
        assertNotNull(SimpleService.supplied, "Should inject supplied object");
    }

    public static class SimpleService {
        private static MockFirstSupplied supplied;

        public void service(MockFirstSupplied supplied) {
            SimpleService.supplied = supplied;
        }
    }

    private enum NoKeys {
    }

    @TestSource
    public static class MockFirstSupplierSource extends AbstractSupplierSource {

        @Override
        protected void loadSpecification(SpecificationContext context) {
        }

        @Override
        public void supply(SupplierSourceContext context) throws Exception {
            context.addManagedObjectSource(null, MockFirstSupplied.class,
                    new AbstractManagedObjectSource<NoKeys, NoKeys>() {
                        @Override
                        protected void loadSpecification(SpecificationContext context) {
                        }

                        @Override
                        protected void loadMetaData(MetaDataContext<NoKeys, NoKeys> context) throws Exception {
                            context.setObjectClass(MockFirstSupplied.class);
                        }

                        @Override
                        protected ManagedObject getManagedObject() throws Throwable {
                            return MockFirstSupplied::new;
                        }
                    });
        }

        @Override
        public void terminate() {
        }
    }

    public static class MockFirstSupplied {
    }

    @Test
    public void properties() throws Throwable {
        MockSecondSupplierSource.capturedProperty = null;
        PropertiesService.supplied = null;
        this.doTest(PropertiesService.class, (architect, properties) ->
                architect.addSupplier("second", "officefloor/suppliers/second.yml", properties));
        assertEquals("test-value", MockSecondSupplierSource.capturedProperty,
                "Should pass properties to supplier");
        assertNotNull(PropertiesService.supplied, "Should inject supplied object");
    }

    public static class PropertiesService {
        private static MockSecondSupplied supplied;

        public void service(MockSecondSupplied supplied) {
            PropertiesService.supplied = supplied;
        }
    }

    @TestSource
    public static class MockSecondSupplierSource extends AbstractSupplierSource {

        static final String PROPERTY_CONFIGURED = "configured.property";
        static String capturedProperty = null;

        @Override
        protected void loadSpecification(SpecificationContext context) {
            context.addProperty(PROPERTY_CONFIGURED, "Configured Property");
        }

        @Override
        public void supply(SupplierSourceContext context) throws Exception {
            capturedProperty = context.getProperty(PROPERTY_CONFIGURED);
            context.addManagedObjectSource(null, MockSecondSupplied.class,
                    new AbstractManagedObjectSource<NoKeys, NoKeys>() {
                        @Override
                        protected void loadSpecification(SpecificationContext context) {
                        }

                        @Override
                        protected void loadMetaData(MetaDataContext<NoKeys, NoKeys> context) throws Exception {
                            context.setObjectClass(MockSecondSupplied.class);
                        }

                        @Override
                        protected ManagedObject getManagedObject() throws Throwable {
                            return MockSecondSupplied::new;
                        }
                    });
        }

        @Override
        public void terminate() {
        }
    }

    public static class MockSecondSupplied {
    }

    @Test
    public void directory() throws Throwable {
        DirectoryService.first = null;
        DirectoryService.second = null;
        this.doTest(DirectoryService.class, (architect, properties) ->
                architect.addSuppliers("officefloor/suppliers", properties));
        assertNotNull(DirectoryService.first, "Should inject first supplied object");
        assertNotNull(DirectoryService.second, "Should inject second supplied object");
    }

    public static class DirectoryService {
        private static MockFirstSupplied first;
        private static MockSecondSupplied second;

        public void service(MockFirstSupplied first, MockSecondSupplied second) {
            DirectoryService.first = first;
            DirectoryService.second = second;
        }
    }

    @FunctionalInterface
    protected interface SetupSuppliers {
        void setup(SupplierArchitect architect, PropertyList properties) throws Exception;
    }

    private void doTest(Class<?> sectionClass, SetupSuppliers setup) throws Throwable {
        CompileOfficeFloor compile = new CompileOfficeFloor();
        compile.office((context) -> {
            OfficeArchitect officeArchitect = context.getOfficeArchitect();
            OfficeSourceContext sourceContext = context.getOfficeSourceContext();
            SupplierArchitect supplierArchitect = SupplierEmployer.employSupplierArchitect(officeArchitect,
                    sourceContext);

            PropertyList properties = sourceContext.createPropertyList();
            properties.addProperty("TestClass").setValue(this.getClass().getName());
            setup.setup(supplierArchitect, properties);

            context.addSection("SECTION", sectionClass);
        });
        try (OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor()) {
            CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.service", null);
        }
    }

}
