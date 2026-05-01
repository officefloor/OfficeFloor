package net.officefloor.activity.admin;

import net.officefloor.activity.admin.build.AdministrationArchitect;
import net.officefloor.activity.admin.build.AdministrationEmployer;
import net.officefloor.activity.compose.build.ComposeArchitect;
import net.officefloor.activity.compose.build.ComposeEmployer;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.administration.source.impl.AbstractAdministrationSource;
import net.officefloor.compile.spi.office.OfficeAdministration;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.plugin.section.clazz.Parameter;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AdministrationTest {

    @Test
    public void simple() throws Throwable {
        MockSimple.managedObjects = null;
        this.doTest((admin, properties) ->
                Map.of("simple", admin.addAdministration("simple", "officefloor/admin/simple.yml", properties)));
        assertNotNull(MockSimple.managedObjects, "Should have administered managed objects");
    }

    public static class MockSimple {
        private static MockManagedObject[] managedObjects;
        public void admin(MockManagedObject[] managedObjects) {
            MockSimple.managedObjects = managedObjects;
        }
    }

    @Test
    public void complex() throws Throwable {
        ComplexAdministrationSource.requiredProperty = null;
        ComplexAdministrationSource.managedObjects = null;
        HandleFlowService.parameter = null;
        this.doTest((admin, properties) ->
                Map.of("complex", admin.addAdministration("complex", "officefloor/admin/complex.yml", properties)));
        assertEquals("configured", ComplexAdministrationSource.requiredProperty, "Can configure properties");
        assertNotNull(ComplexAdministrationSource.managedObjects, "Should have administered managed objects");
        assertEquals("TEST", HandleFlowService.parameter, "Should handle flow");
    }

    public static enum FlowKeys {
        HANDLE_FLOW
    }

    public static class ComplexAdministrationSource extends AbstractAdministrationSource<MockManagedObject, FlowKeys, None>
            implements AdministrationFactory<MockManagedObject, FlowKeys, None>, Administration<MockManagedObject, FlowKeys, None> {

        private static String requiredProperty;

        private static MockManagedObject[] managedObjects;

        @Override
        protected void loadSpecification(SpecificationContext context) {
            context.addProperty("required.property", "Required");
        }

        @Override
        protected void loadMetaData(MetaDataContext<MockManagedObject, FlowKeys, None> context) throws Exception {
            requiredProperty = context.getAdministrationSourceContext().getProperty("required.property");
            context.setExtensionInterface(MockManagedObject.class);
            context.setAdministrationFactory(this);
        }

        @Override
        public Administration<MockManagedObject, FlowKeys, None> createAdministration() throws Throwable {
            return this;
        }

        @Override
        public void administer(AdministrationContext<MockManagedObject, FlowKeys, None> context) throws Throwable {
            managedObjects = context.getExtensions();
            context.doFlow(FlowKeys.HANDLE_FLOW, "TEST", null);
        }
    }

    public static class HandleFlowService {
        private static String parameter;
        public void service(@Parameter String parameter) {
            HandleFlowService.parameter = parameter;
        }
    }

    @Test
    public void directory() throws Throwable {
        MockSimple.managedObjects = null;
        ComplexAdministrationSource.requiredProperty = null;
        ComplexAdministrationSource.managedObjects = null;
        HandleFlowService.parameter = null;
        this.doTest((admin, properties) ->
                admin.addAdministrations("officefloor/admin", properties));
        assertNotNull(MockSimple.managedObjects, "Should have administered managed objects");
        assertEquals("configured", ComplexAdministrationSource.requiredProperty, "Can configure properties");
        assertNotNull(ComplexAdministrationSource.managedObjects, "Should have administered managed objects");
        assertEquals("TEST", HandleFlowService.parameter, "Should handle flow");
    }

    @FunctionalInterface
    protected static interface SetupAdministration {
        Map<String, OfficeAdministration> setup(AdministrationArchitect adminArchitect,
                                                PropertyList properties) throws Exception;
    }

    private void doTest(SetupAdministration setup) throws Throwable {
        CompileOfficeFloor compile = new CompileOfficeFloor();
        compile.office((office) -> {

            // Employ architects
            OfficeArchitect officeArchitect = office.getOfficeArchitect();
            OfficeSourceContext sourceContext = office.getOfficeSourceContext();
            ComposeArchitect composeArchitect = ComposeEmployer.employComposeArchitect(officeArchitect, sourceContext);
            AdministrationArchitect adminArchitect = AdministrationEmployer.employAdministrationArchitect(officeArchitect, composeArchitect, sourceContext);

            // Add servicing
            OfficeSection service = office.addSection("service", MockService.class);
            office.addManagedObject("mo", MockManagedObject.class, ManagedObjectScope.THREAD);

            // Add the managed object
            office.addManagedObject("MO", MockManagedObject.class, ManagedObjectScope.THREAD);

            // Setup the administration
            PropertyList properties = sourceContext.createPropertyList();
            properties.addProperty("TestClass").setValue(this.getClass().getName());
            Map<String, OfficeAdministration> adminMap = setup.setup(adminArchitect, properties);

            // Provide in alphabetical order
            List<OfficeAdministration> admins = adminMap.keySet().stream().sorted().map(adminMap::get).toList();

            // Configure pre-administration
            office.getOfficeArchitect().addManagedFunctionAugmentor((augmentContext) -> {
                for (OfficeAdministration admin : admins) {
                    augmentContext.addPreAdministration(admin);
                }
            });
        });
        try (OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor()) {
            CompileOfficeFloor.invokeProcess(officeFloor, "service.service", null);
        }
    }

    public static class MockService {
        public void service() {
        }
    }

    public static class MockManagedObject {
    }

}
