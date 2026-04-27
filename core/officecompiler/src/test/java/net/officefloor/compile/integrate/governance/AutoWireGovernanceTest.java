package net.officefloor.compile.integrate.governance;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.plugin.governance.clazz.ClassGovernanceSource;
import net.officefloor.plugin.governance.clazz.Enforce;
import net.officefloor.plugin.governance.clazz.Govern;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AutoWireGovernanceTest {

    @Test
    public void governSingleManagedObjects() throws Throwable {
        CompileOfficeFloor compile = new CompileOfficeFloor();
        compile.office((context) -> {
            OfficeArchitect architect = context.getOfficeArchitect();

            // Configure the section and managed objects
            OfficeSection section = context.addSection("SECTION", MockSingle.class);
            context.addManagedObject("MO", MockManagedObjectOne.class, ManagedObjectScope.THREAD);

            // Configure the governance
            OfficeGovernance governance = architect
                    .addOfficeGovernance("governance", ClassGovernanceSource.class.getName());
            governance.addProperty(ClassGovernanceSource.CLASS_NAME_PROPERTY_NAME, MockGovernance.class.getName());
            governance.enableAutoWireExtensions();
            section.addGovernance(governance);
        });
        try (OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor()) {
            MockGovernance.governedObjects.clear();
            CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.service", null);
            assertEquals(1, MockGovernance.governedObjects.size(), "Should govern both managed object");
            assertInstanceOf(MockManagedObjectOne.class, MockGovernance.governedObjects.get(0), "Should govern managed object one");
        }
    }

    @Test
    public void governMultipleManagedObjects() throws Throwable {
        CompileOfficeFloor compile = new CompileOfficeFloor();
        compile.office((context) -> {
            OfficeArchitect architect = context.getOfficeArchitect();

            // Configure the section and managed objects
            OfficeSection section = context.addSection("SECTION", MockMultiple.class);
            context.addManagedObject("ONE", MockManagedObjectOne.class, ManagedObjectScope.THREAD);
            context.addManagedObject("TWO", MockManagedObjectTwo.class, ManagedObjectScope.THREAD);

            // Configure the governance
            OfficeGovernance governance = architect
                    .addOfficeGovernance("governance", ClassGovernanceSource.class.getName());
            governance.addProperty(ClassGovernanceSource.CLASS_NAME_PROPERTY_NAME, MockGovernance.class.getName());
            governance.enableAutoWireExtensions();
            section.addGovernance(governance);
        });
        try (OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor()) {
            MockGovernance.governedObjects.clear();
            CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.service", null);
            assertEquals(2, MockGovernance.governedObjects.size(), "Should govern both managed object");
            boolean isOneAvailable = false;
            boolean isTwoAvailable = false;
            for (CommonType common : MockGovernance.governedObjects) {
                if (common instanceof MockManagedObjectOne) {
                    isOneAvailable = true;
                } else if (common instanceof MockManagedObjectTwo) {
                    isTwoAvailable = true;
                }
            }
            assertTrue(isOneAvailable, "Managed object one should be governed");
            assertTrue(isTwoAvailable, "Managed object two should be governed");
        }
    }

    public static class MockSingle {
        public void service(MockManagedObjectOne one) {
        }
    }

    public static class MockMultiple {
        public void service(MockManagedObjectOne one, MockManagedObjectTwo two) {
        }
    }

    public static interface CommonType {
    }

    public static class MockManagedObjectOne implements CommonType {
    }

    public static class MockManagedObjectTwo implements CommonType {
    }

    public static class MockGovernance {

        private static List<CommonType> governedObjects = new ArrayList<>(2);

        @Govern
        public void govern(CommonType extension) {
            governedObjects.add(extension);
        }

        @Enforce
        public void enforce() {
        }
    }

}
