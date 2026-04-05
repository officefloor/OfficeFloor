package net.officefloor.activity.compose;

import lombok.Data;
import net.officefloor.activity.compose.build.ComposeBuilder;
import net.officefloor.activity.compose.build.ComposeEmployer;
import net.officefloor.activity.compose.build.ComposeSource;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.InputManagedObject;
import net.officefloor.frame.api.managedobject.recycle.CleanupEscalation;
import net.officefloor.frame.test.Closure;
import net.officefloor.plugin.section.clazz.Parameter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Tests the {@link net.officefloor.activity.compose.build.ComposeBuilder}
 */
public class ComposeBuilderTest {

    @Test
    public void build() throws Throwable {
        final String ITEM = "ITEM";
        this.doTest("build.yml", ComposeConfig.class, (context) -> {
            return ITEM;
        }, (item, officeFloor) -> {
            assertSame(ITEM, item, "Incorrect item");
        });
    }

    @Test
    public void composeUsage() throws Throwable {
        this.doTest("compose.yml", ComposeConfig.class, (context) -> {

            // Ensure correct configuration
            ComposeConfig configuration = context.getConfiguration();
            String startFunctionName = configuration.getStart();
            assertEquals("start", startFunctionName, "Incorrect start function name");

            // Ensure correct input
            OfficeSectionInput input = context.getStartFunction();
            assertEquals("start", input.getOfficeSectionInputName(), "Incorrect start input");

            // Invoke input
            return input.addExternalServiceInput(Integer.class, MockManagedObject.class);

        }, (item, officeFloor) -> {
            StartService.parameter = null;
            final Integer PARAMETER = 1;
            item.service(new MockManagedObject<>(PARAMETER), null);
            assertSame(PARAMETER, StartService.parameter, "Incorrect parameter");
        });
    }

    public static class StartService {
        public static Integer parameter = null;

        public void service(@Parameter Integer parameter) {
            StartService.parameter = parameter;
        }
    }

    public static class MockManagedObject<T> implements InputManagedObject {

        private final T item;

        public MockManagedObject(T item) {
            this.item = item;
        }

        @Override
        public Object getObject() throws Throwable {
            return item;
        }

        @Override
        public void clean(CleanupEscalation[] cleanupEscalations) throws Throwable {
            // Nothing to clean up
        }
    }

    @Test
    public void linkToAdditionalFunction() throws Throwable {
        this.doTest("link.yaml", ComposeConfig.class, (context) -> {

            // Ensure can link to additional function
            OfficeSectionInput link = context.getFunction("link");

            // Look to invoke the function
            return link.addExternalServiceInput(String.class, MockManagedObject.class);

        }, (item, officeFloor) -> {
            LinkService.parameter = null;
            final String PARAMETER = "Test";
            item.service(new MockManagedObject<>(PARAMETER), null);
            assertEquals(PARAMETER, LinkService.parameter, "Should invoke linked service");
        });
    }

    public static class LinkService {
        public static String parameter = null;

        public void service(@Parameter String parameter) {
            LinkService.parameter = parameter;
        }
    }

    @Test
    public void extendedConfiguration() throws Throwable {
        this.doTest("extend.yml", TestComposeConfig.class, (context) -> {

            // Ensure the correct extended configuration
            TestComposeConfig configuration = context.getConfiguration();
            assertEquals("EXTENDED", configuration.getExtendConfiguration(), "Should have extended configuration");

            return null;
        }, null);
    }

    @Data
    public static class TestComposeConfig extends ComposeConfig {
        private String extendConfiguration;
    }

    @Test
    public void alterConfiguration() throws Throwable {
        this.doTest("empty.yml", ComposeConfig.class, (context) -> {

            // Alter configuration by added a function
            // (allows altering/validating composition before it's loaded)
            ComposeConfig configuration = context.getConfiguration();
            FunctionConfig added = new FunctionConfig();
            added.setClassName(AddedService.class.getName());
            configuration.getFunctions().put("added", added);

            // Allow invoking the added function
            return context.getFunction("added").addExternalServiceInput(Integer.class, MockManagedObject.class);

        }, (item, officeFloor) -> {
            AddedService.parameter = 0;
            final int PARAMETER = 1;
            item.service(new MockManagedObject<>(PARAMETER), null);
            assertEquals(PARAMETER, AddedService.parameter, "Function should be invoked");
        });
    }

    public static class AddedService {
        public static int parameter = 0;

        public void service(@Parameter Integer parameter) {
            AddedService.parameter = parameter;
        }
    }

    public static interface TestLogic<T> {
        void handle(T item, OfficeFloor officeFloor);
    }

    private <T, C extends ComposeConfig> void doTest(String resourceName, Class<C> configurationClass,
                                                     ComposeSource<T, C> source, TestLogic<T> test) throws Throwable {

        // Compile and capture the item
        Closure<T> item = new Closure<>();
        CompileOfficeFloor compiler = new CompileOfficeFloor();
        compiler.office((office) -> {
            ComposeBuilder composeBuilder = ComposeEmployer.employComposeBuilder(office.getOfficeArchitect(), office.getOfficeSourceContext());

            // Add the composition
            PropertyList properties = office.getOfficeSourceContext().createPropertyList();
            properties.addProperty("TestClass").setValue(this.getClass().getName());
            item.value = composeBuilder.build("compose", source, "builder/" + resourceName, properties, configurationClass);
        });

        // Test
        try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {
            if (test != null) {
                test.handle(item.value, officeFloor);
            }
        }
    }

}
