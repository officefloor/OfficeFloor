package net.officefloor.activity.compose;

import net.officefloor.activity.compose.build.ComposeArchitect;
import net.officefloor.activity.compose.build.ComposeEmployer;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.governance.source.impl.AbstractGovernanceSource;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.governance.GovernanceContext;
import net.officefloor.frame.api.governance.GovernanceFactory;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.InputManagedObject;
import net.officefloor.frame.api.managedobject.recycle.CleanupEscalation;
import net.officefloor.frame.test.Closure;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ComposeGovernanceTest {

    @Test
    public void single() throws Throwable {
        final String NAME = "SINGLE";
        MockManagedObject mo = this.doTest("single.yml", (office, compose) -> {
            OfficeGovernance governance = office.addOfficeGovernance("single", new MockGovernanceSource(NAME));
            governance.enableAutoWireExtensions();
            compose.addGovernance("single", governance);
        });
        assertEquals(1, mo.governances.size(), "Should be governed");
        assertEquals(NAME, mo.governances.get(0), "Incorrect governance");
    }

    @Test
    public void multiple() throws Throwable {
        final int GOVERNANCE_COUNT = 10;
        MockManagedObject mo = this.doTest("multiple.yml", (office, compose) -> {
           for (int i = 0 ; i < GOVERNANCE_COUNT; i++) {
               OfficeGovernance governance = office.addOfficeGovernance("govern" + i, new MockGovernanceSource("governance_" + i));
               governance.enableAutoWireExtensions();
               compose.addGovernance("governance_" + i, governance);
           }
        });
        assertEquals(10, mo.governances.size(), "Should be governed");
        for (int i = 0; i < GOVERNANCE_COUNT; i++) {
            assertEquals("governance_" + i, mo.governances.get(i), "Incorrect governance " + i);
        }
    }

    @FunctionalInterface
    public static interface TestSetup {
        void setup(OfficeArchitect officeArchitect, ComposeArchitect composeArchitect);
    }

    private <T, C extends ComposeConfiguration> MockManagedObject doTest(String resourceName, TestSetup setup) throws Throwable {
        Closure<ExternalServiceInput<MockManagedObject, MockManagedObject>> item = new Closure<>();
        CompileOfficeFloor compiler = new CompileOfficeFloor();
        compiler.office((office) -> {
            OfficeArchitect officeArchitect = office.getOfficeArchitect();
            ComposeArchitect composeArchitect = ComposeEmployer.employComposeArchitect(officeArchitect, office.getOfficeSourceContext());

            // Setup
            setup.setup(officeArchitect, composeArchitect);

            // Add the composition
            PropertyList properties = office.getOfficeSourceContext().createPropertyList();
            properties.addProperty("TestClass").setValue(this.getClass().getName());
            item.value = composeArchitect.addComposition("compose",
                    (context) -> context.getStartFunction().addExternalServiceInput(MockManagedObject.class, MockManagedObject.class),
                    "governance/" + resourceName, properties, ComposeConfiguration.class);
        });
        try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {
            MockManagedObject mo = new MockManagedObject();
            item.value.service(mo, null);
            return mo;
        }
    }

    public static class GovernedService {
        public void service(MockManagedObject mo) {
        }
    }

    public static class MockManagedObject implements InputManagedObject {

        public final List<String> governances = new LinkedList<>();

        @Override
        public Object getObject() throws Throwable {
            return this;
        }

        @Override
        public void clean(CleanupEscalation[] cleanupEscalations) throws Throwable {
            // Nothing to clean up
        }
    }

    public static class MockGovernanceSource extends AbstractGovernanceSource<MockManagedObject, None>
            implements GovernanceFactory<MockManagedObject, None>, Governance<MockManagedObject, None> {

        private final String name;

        public MockGovernanceSource(String name) {
            this.name = name;
        }

        @Override
        protected void loadSpecification(SpecificationContext context) {
            // No specification
        }

        @Override
        protected void loadMetaData(MetaDataContext<MockManagedObject, None> context) throws Exception {
            context.setExtensionInterface(MockManagedObject.class);
            context.setGovernanceFactory(this);
        }

        @Override
        public Governance<MockManagedObject, None> createGovernance() throws Throwable {
            return this;
        }

        @Override
        public void governManagedObject(MockManagedObject managedObjectExtension, GovernanceContext<None> context) throws Throwable {
            managedObjectExtension.governances.add(this.name);
        }

        @Override
        public void enforceGovernance(GovernanceContext<None> context) throws Throwable {
        }

        @Override
        public void disregardGovernance(GovernanceContext<None> context) throws Throwable {
        }
    }

}
