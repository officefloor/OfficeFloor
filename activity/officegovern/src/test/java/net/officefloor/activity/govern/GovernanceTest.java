package net.officefloor.activity.govern;

import net.officefloor.activity.compose.build.ComposeArchitect;
import net.officefloor.activity.compose.build.ComposeEmployer;
import net.officefloor.activity.govern.build.GovernanceArchitect;
import net.officefloor.activity.govern.build.GovernanceEmployer;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.governance.source.impl.AbstractGovernanceSource;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.governance.GovernanceContext;
import net.officefloor.frame.api.governance.GovernanceFactory;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.plugin.governance.clazz.Enforce;
import net.officefloor.plugin.governance.clazz.Govern;
import net.officefloor.plugin.section.clazz.Parameter;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GovernanceTest {

    @Test
    public void simple() throws Throwable {
        this.doTest(((governArchitect, properties, service) -> {
            OfficeGovernance governance = governArchitect.addGovernance("simple", "officefloor/govern/simple.yml", properties);
            service.addGovernance(governance);
        }), SimpleGovernance.class);
    }

    public static class SimpleGovernance {

        @Govern
        public void govern(MockManagedObject managedObject) {
            managedObject.governances.add(this);
        }

        @Enforce
        public void enforce() {
        }
    }

    @Test
    public void complete() throws Throwable {
        this.doTest(((governArchitect, properties, service) -> {
            OfficeGovernance governance = governArchitect.addGovernance("complete", "officefloor/govern/complete.yml", properties);
            service.addGovernance(governance);
        }), CompleteGovernance.class);
    }

    @TestSource
    public static class CompleteGovernance extends AbstractGovernanceSource<MockManagedObject, CompleteGovernance.FlowKeys>
            implements GovernanceFactory<MockManagedObject, CompleteGovernance.FlowKeys>, Governance<MockManagedObject, CompleteGovernance.FlowKeys> {

        public static enum FlowKeys {
            FLOW
        }

        @Override
        protected void loadSpecification(SpecificationContext context) {
            context.addProperty("property");
        }

        @Override
        protected void loadMetaData(MetaDataContext<MockManagedObject, FlowKeys> context) throws Exception {

            // Ensure provide configuration
            assertEquals("available", context.getGovernanceSourceContext().getProperty("property"), "Should configure property");

            // Configure meta-data
            context.setExtensionInterface(MockManagedObject.class);
            context.setGovernanceFactory(this);
            context.addFlow(FlowKeys.FLOW, null);
            context.addEscalation(IOException.class);
            context.addEscalation(SQLException.class);
        }

        @Override
        public Governance<MockManagedObject, FlowKeys> createGovernance() throws Throwable {
            return this;
        }

        @Override
        public void governManagedObject(MockManagedObject managedObjectExtension, GovernanceContext<FlowKeys> context) throws Throwable {
            managedObjectExtension.governances.add(this);
        }

        @Override
        public void enforceGovernance(GovernanceContext<FlowKeys> context) throws Throwable {
            // Only testing setup
        }

        @Override
        public void disregardGovernance(GovernanceContext<FlowKeys> context) throws Throwable {
            // Only testing setup
        }
    }

    public static class LogicService {
        public void service() {
        }
    }

    public static class IoHandlerService {
        public void handle(@Parameter IOException ex) {
        }
    }

    public static class SqlHandlerService {
        public void handle(@Parameter SQLException ex) {
        }
    }

    @Test
    public void directory() throws Throwable {
        this.doTest(((governArchitect, properties, service) -> {
            Map<String, OfficeGovernance> governances = governArchitect.addGovernances("officefloor/govern", properties);
            OfficeGovernance simple = governances.get("simple");
            assertNotNull(simple, "Should have simple governance");
            OfficeGovernance complete = governances.get("complete");
            assertNotNull(complete, "Should have complete governance");
            service.addGovernance(simple);
            service.addGovernance(complete);
        }), CompleteGovernance.class, SimpleGovernance.class);

    }

    @FunctionalInterface
    protected static interface SetupGovernance {
        void setup(GovernanceArchitect governArchitect,
                                PropertyList properties, OfficeSection service) throws Exception;
    }

    private void doTest(SetupGovernance setup, Class<?>... expectedGoverance) throws Throwable {
        CompileOfficeFloor compile = new CompileOfficeFloor();
        compile.office((office) -> {

            // Employ architects
            OfficeArchitect officeArchitect = office.getOfficeArchitect();
            OfficeSourceContext sourceContext = office.getOfficeSourceContext();
            ComposeArchitect composeArchitect = ComposeEmployer.employComposeArchitect(officeArchitect, sourceContext);
            GovernanceArchitect governArchitect = GovernanceEmployer.employGovernanceArchitect(officeArchitect, composeArchitect, sourceContext);

            // Add servicing
            OfficeSection service = office.addSection("service", MockService.class);
            office.addManagedObject("mo", MockManagedObject.class, ManagedObjectScope.THREAD);

            // Setup the governance
            PropertyList properties = sourceContext.createPropertyList();
            properties.addProperty("TestClass").setValue(this.getClass().getName());
            setup.setup(governArchitect, properties, service);
        });
        try (OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor()) {
            MockService.governances = null;
            CompileOfficeFloor.invokeProcess(officeFloor, "service.service", null);
            assertEquals(expectedGoverance.length, MockService.governances.size(), "Incorrect number of governances");
            for (int i = 0; i < expectedGoverance.length; i++) {
                assertEquals(expectedGoverance[i], MockService.governances.get(i).getClass(), "Incorrect governance " + i);
            }
        }
    }

    public static class MockService {
        public static List<Object> governances = null;
        public void service(MockManagedObject mo) {
            governances = mo.governances;
        }
    }

    public static class MockManagedObject {
        public List<Object> governances = new LinkedList<>();
    }

}
