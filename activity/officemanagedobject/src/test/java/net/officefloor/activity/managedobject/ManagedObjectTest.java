package net.officefloor.activity.managedobject;

import net.officefloor.activity.managedobject.build.ManagedObjectArchitect;
import net.officefloor.activity.managedobject.build.ManagedObjectEmployer;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.section.clazz.Parameter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class ManagedObjectTest {

    @Test
    public void simple() throws Throwable {
        SimpleService.managedObject = null;
        this.doTest(SimpleService.class, (moArchitect, properties) ->
                moArchitect.addManagedObject("MO", "officefloor/managedobjects/simple.yml", properties));
        assertNotNull(SimpleService.managedObject, "Should have injected managed object");
    }

    public static class SimpleService {
        private static MockSimple managedObject;
        public void service(MockSimple managedObject) {
            SimpleService.managedObject = managedObject;
        }
    }

    public static class MockSimple {
    }

    @Test
    public void complex() throws Throwable {
        ComplexManagedObjectSource.requiredProperty = null;
        ComplexManagedObjectSource.dependency = null;
        FlowService.parameter = null;
        ComplexService.managedObject = null;
        this.doTest(ComplexService.class, ((moArchitect, properties) ->
                moArchitect.addManagedObject("MO", "officefloor/managedobjects/complex.yml", properties)));
        assertEquals(ComplexManagedObjectSource.requiredProperty, "configured", "Should provide property");
        assertNotNull(ComplexManagedObjectSource.dependency, "Should load dependency");
        assertEquals("TEST", FlowService.parameter, "Should invoke flow");
        assertNotNull(ComplexService.managedObject, "Should have injected managed object");
    }

    public static class ComplexService {
        private static ComplexManagedObjectSource managedObject;
        public void service(ComplexManagedObjectSource managedObject) {
            ComplexService.managedObject = managedObject;
        }
    }

    public static class FlowService {
        private static String parameter;
        public void service(@Parameter String parameter) {
            FlowService.parameter = parameter;
        }
    }

    public static enum DependencyKeys {
        MOCK_SIMPLE
    }

    public static enum FlowKeys {
        HANDLE_FLOW
    }

    public static class ComplexManagedObjectSource extends AbstractManagedObjectSource<DependencyKeys, FlowKeys>
            implements CoordinatingManagedObject<DependencyKeys> {

        private static String requiredProperty = null;

        private static MockSimple dependency = null;

        @Override
        protected void loadSpecification(SpecificationContext context) {
            context.addProperty("required.property", "Required");
        }

        @Override
        protected void loadMetaData(MetaDataContext<DependencyKeys, FlowKeys> context) throws Exception {
            requiredProperty = context.getManagedObjectSourceContext().getProperty("required.property");
            context.setObjectClass(this.getClass());
            context.setManagedObjectClass(this.getClass());
            context.addDependency(DependencyKeys.MOCK_SIMPLE, MockSimple.class);
            context.addFlow(FlowKeys.HANDLE_FLOW, String.class);
        }

        @Override
        public void start(ManagedObjectExecuteContext<FlowKeys> context) throws Exception {
            context.invokeStartupProcess(FlowKeys.HANDLE_FLOW, "TEST", this, null);
        }

        @Override
        protected ManagedObject getManagedObject() throws Throwable {
            return this;
        }

        @Override
        public void loadObjects(ObjectRegistry<DependencyKeys> registry) throws Throwable {
            dependency = (MockSimple) registry.getObject(DependencyKeys.MOCK_SIMPLE);
        }

        @Override
        public Object getObject() throws Throwable {
            return this;
        }
    }

    @Test
    public void directory() throws Throwable {
        DirectoryService.simple = null;
        DirectoryService.complex = null;
        this.doTest(DirectoryService.class, ((moArchitect, properties) ->
                moArchitect.addManagedObjects("officefloor/managedobjects", properties)));
        assertNotNull(DirectoryService.simple, "Simple should be injected");
        assertNotNull(DirectoryService.complex, "Complex should be injected");
        assertSame(ComplexManagedObjectSource.dependency, DirectoryService.simple, "Should be same object injected");
    }

    public static class DirectoryService {
        private static MockSimple simple;
        private static ComplexManagedObjectSource complex;
        public void service(MockSimple simple, ComplexManagedObjectSource complex) {
            DirectoryService.simple = simple;
            DirectoryService.complex = complex;
        }
    }

    @FunctionalInterface
    protected static interface SetupManagedObjects {
        void setup(ManagedObjectArchitect moArchitect, PropertyList propertyList) throws Exception;
    }

    private void doTest(Class<?> sectionClass, SetupManagedObjects setup) throws Throwable {
        CompileOfficeFloor compile = new CompileOfficeFloor();
        compile.office((context) -> {

            // Obtain the managed object architect
            OfficeSourceContext sourceContext = context.getOfficeSourceContext();
            ManagedObjectArchitect moArchitect = ManagedObjectEmployer.employManagedObjectArchitect(context.getOfficeArchitect(), context.getOfficeSourceContext());

            // Add the managed object
            PropertyList properties = sourceContext.createPropertyList();
            properties.addProperty("TestClass").setValue(this.getClass().getName());
            setup.setup(moArchitect, properties);

            // Include function to use managed object
            context.addSection("SECTION", sectionClass);
        });
        try (OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor()) {
            CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.service", null);
        }
    }

}
