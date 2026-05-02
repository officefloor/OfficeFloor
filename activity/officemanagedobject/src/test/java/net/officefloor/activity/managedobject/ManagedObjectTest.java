package net.officefloor.activity.managedobject;

import net.officefloor.activity.compose.build.ComposeArchitect;
import net.officefloor.activity.compose.build.ComposeEmployer;
import net.officefloor.activity.managedobject.build.ManagedObjectArchitect;
import net.officefloor.activity.managedobject.build.ManagedObjectEmployer;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.plugin.clazz.Qualified;
import net.officefloor.plugin.section.clazz.Parameter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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
    public void dependency() throws Throwable {
        DependencyService.managedObject = null;
        this.doTest(DependencyService.class, ((moArchitect, properties) -> {
            moArchitect.addManagedObject("simple", "officefloor/managedobjects/simple.yml", properties);
            moArchitect.addManagedObject("dependency", "officefloor/managedobjects/dependency.yml", properties);
        }));
        assertNotNull(DependencyService.managedObject, "Should have injected managed object");
        assertNotNull(DependencyService.managedObject.simple, "Should have dependency injected");
    }

    public static class DependencyService {
        private static MockDependency managedObject;
        public void service(MockDependency managedObject) {
            DependencyService.managedObject = managedObject;
        }
    }

    public static class MockDependency {
        private @Dependency MockSimple simple;
    }

    @Test
    public void qualifiedManagedObject() throws Throwable {
        QualifiedManagedObjectService.managedObject = null;
        this.doTest(QualifiedManagedObjectService.class, ((moArchitect, properties) -> {
            moArchitect.addManagedObject("qualified-one", "officefloor/managedobjects/qualified-one.yml", properties);
            moArchitect.addManagedObject("qualified-two", "officefloor/managedobjects/qualified-two.yml", properties);
        }));
        assertInstanceOf(MockQualifiedManagedObjectOne.class, QualifiedManagedObjectService.managedObject, "Should inject qualified managed object");
    }

    public static class QualifiedManagedObjectService {
        private static MockQualifiedManagedObject managedObject;
        public void service(@Qualified("qualified-one") MockQualifiedManagedObject managedObject) {
            QualifiedManagedObjectService.managedObject = managedObject;
        }
    }

    public static interface MockQualifiedManagedObject {
    }

    public static class MockQualifiedManagedObjectOne implements MockQualifiedManagedObject {
    }

    public static class MockQualifiedManagedObjectTwo implements MockQualifiedManagedObject {
    }

    @Test
    public void qualifiedDependency() throws Throwable {
        QualifiedDependencyService.managedObject = null;
        this.doTest(QualifiedDependencyService.class, ((moArchitect, properties) -> {
            moArchitect.addManagedObject("qualified-one", "officefloor/managedobjects/qualified-one.yml", properties);
            moArchitect.addManagedObject("qualified-two", "officefloor/managedobjects/qualified-two.yml", properties);
            moArchitect.addManagedObject("qualified-dependency", "officefloor/managedobjects/qualified-dependency.yml", properties);
        }));
        assertNotNull(QualifiedDependencyService.managedObject, "Should inject managed object");
        assertInstanceOf(MockQualifiedManagedObjectTwo.class, QualifiedDependencyService.managedObject.dependency, "Should inject qualified managed object");
    }

    public static class QualifiedDependencyService {
        public static MockQualifiedDependency managedObject;
        public void service(MockQualifiedDependency managedObject) {
            QualifiedDependencyService.managedObject = managedObject;
        }
    }

    public static class MockQualifiedDependency {
        private @Qualified("qualified-two") @Dependency MockQualifiedManagedObject dependency;
    }

    @Test
    public void input() throws Throwable {
        MockInputManagedObjectSource.requiredProperty = null;
        FlowService.parameter = null;
        InputService.managedObject = null;
        this.doTest(InputService.class, ((moArchitect, properties) -> {
                moArchitect.addManagedObject("input", "officefloor/managedobjects/input.yml", properties);
                moArchitect.addManagedObject("input-dependency", "officefloor/managedobjects/input-dependency.yml", properties);
            }));
        assertEquals("configured", MockInputManagedObjectSource.requiredProperty, "Should provide property");
        assertNotNull(InputService.managedObject, "Should have injected managed object");
        assertNotNull(InputService.managedObject.dependency, "Should inject input managed object dependency");
        assertEquals("TEST", FlowService.parameter, "Should invoke flow");
        assertNotNull(FlowService.managedObject, "Should provide input managed object");
        assertNotNull(FlowService.managedObject.dependency, "Should inject flow invoked managed object dependency");
    }

    public static class InputService {
        private static MockInputManagedObjectSource managedObject;
        public void service(MockInputManagedObjectSource managedObject) {
            InputService.managedObject = managedObject;
        }
    }

    public static class FlowService {
        private static String parameter;
        private static MockInputManagedObjectSource managedObject;
        public void service(@Parameter String parameter, MockInputManagedObjectSource managedObject) {
            FlowService.parameter = parameter;
            FlowService.managedObject = managedObject;
        }
    }

    public static class MockInputDependency {
    }

    public static enum DependencyKeys {
        MOCK_INPUT_DEPENDENCY
    }

    public static enum FlowKeys {
        HANDLE_FLOW
    }

    @TestSource
    public static class MockInputManagedObjectSource extends AbstractManagedObjectSource<DependencyKeys, FlowKeys>
            implements CoordinatingManagedObject<DependencyKeys> {

        private static String requiredProperty = null;

        private MockInputDependency dependency = null;

        @Override
        protected void loadSpecification(SpecificationContext context) {
            context.addProperty("required.property", "Required");
        }

        @Override
        protected void loadMetaData(MetaDataContext<DependencyKeys, FlowKeys> context) throws Exception {
            requiredProperty = context.getManagedObjectSourceContext().getProperty("required.property");
            context.setObjectClass(this.getClass());
            context.setManagedObjectClass(this.getClass());
            context.addDependency(DependencyKeys.MOCK_INPUT_DEPENDENCY, MockInputDependency.class);
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
            this.dependency = (MockInputDependency) registry.getObject(DependencyKeys.MOCK_INPUT_DEPENDENCY);
        }

        @Override
        public Object getObject() throws Throwable {
            return this;
        }
    }

    @Test
    public void directory() throws Throwable {
        DirectoryService.simple = null;
        DirectoryService.dependency = null;
        DirectoryService.qualifiedManagedObject = null;
        DirectoryService.qualifiedDependency = null;
        DirectoryService.input = null;
        this.doTest(DirectoryService.class, ((moArchitect, properties) ->
                moArchitect.addManagedObjects("officefloor/managedobjects", properties)));
        assertNotNull(DirectoryService.simple, "Simple should be injected");
        assertNotNull(DirectoryService.dependency, "Dependency should be injected");
        assertSame(DirectoryService.simple, DirectoryService.dependency.simple, "Should be same object injected");
        assertInstanceOf(MockQualifiedManagedObjectOne.class, DirectoryService.qualifiedManagedObject, "Should inject correct qualified managed object");
        assertNotNull(DirectoryService.qualifiedDependency, "Should inject managed object with qualified dependency");
        assertInstanceOf(MockQualifiedManagedObjectTwo.class, DirectoryService.qualifiedDependency.dependency, "Should inject appropriate qualified managed object dependency");
        assertNotNull(DirectoryService.input, "Input should be injected");
        assertNotNull(DirectoryService.input.dependency, "Should inject input dependency");
    }

    public static class DirectoryService {
        private static MockSimple simple;
        private static MockDependency dependency;
        private static MockQualifiedManagedObject qualifiedManagedObject;
        private static MockQualifiedDependency qualifiedDependency;
        private static MockInputManagedObjectSource input;
        public void service(MockSimple simple, MockDependency dependency,
                            @Qualified("qualified-one") MockQualifiedManagedObject qualifiedManagedObject,
                            MockQualifiedDependency qualifiedDependency,
                            MockInputManagedObjectSource input) {
            DirectoryService.simple = simple;
            DirectoryService.dependency = dependency;
            DirectoryService.qualifiedManagedObject = qualifiedManagedObject;
            DirectoryService.qualifiedDependency = qualifiedDependency;
            DirectoryService.input = input;
        }
    }

    @FunctionalInterface
    protected static interface SetupManagedObjects {
        void setup(ManagedObjectArchitect moArchitect, PropertyList propertyList) throws Exception;
    }

    private void doTest(Class<?> sectionClass, SetupManagedObjects setup) throws Throwable {
        CompileOfficeFloor compile = new CompileOfficeFloor();
        compile.office((context) -> {

            // Load the architects
            OfficeArchitect officeArchitect = context.getOfficeArchitect();
            OfficeSourceContext sourceContext = context.getOfficeSourceContext();
            ComposeArchitect composeArchitect = ComposeEmployer.employComposeArchitect(officeArchitect, sourceContext);
            ManagedObjectArchitect moArchitect = ManagedObjectEmployer.employManagedObjectArchitect(officeArchitect, composeArchitect, sourceContext);

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
