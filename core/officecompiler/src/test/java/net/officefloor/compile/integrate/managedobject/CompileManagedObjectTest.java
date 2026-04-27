package net.officefloor.compile.integrate.managedobject;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.plugin.managedobject.singleton.Singleton;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

public class CompileManagedObjectTest {

    private static MockObject object;

    @Test
    public void managedObjectScope_PROCESS() throws Throwable {
        this.doManagedObjectScopeTest(ManagedObjectScope.PROCESS);
    }

    @Test
    public void managedObjectScope_THREAD() throws Throwable {
        this.doManagedObjectScopeTest(ManagedObjectScope.THREAD);
    }

    @Test
    public void managedObjectScope_FUNCTION() throws Throwable {
        this.doManagedObjectScopeTest(ManagedObjectScope.FUNCTION);
    }

    private void doManagedObjectScopeTest(ManagedObjectScope managedObjectScope) throws Throwable {

        // Create the mock object
        MockObject mockObject = new MockObject();

        // Compile
        CompileOfficeFloor compile = new CompileOfficeFloor();
        compile.office((context) -> {
            OfficeArchitect architect = context.getOfficeArchitect();

            // Add the managed object
            OfficeManagedObject managedObject = architect
                    .addOfficeManagedObjectSource("MOS", new Singleton(mockObject))
                    .addOfficeManagedObject("MO", managedObjectScope);

            // Add function to use managed object
            OfficeSection section = context.addSection("SECTION", MockFunction.class);

            // Link object to managed object
            architect.link(section.getOfficeSectionObject(MockObject.class.getName()), managedObject);
        });
        try (OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor()) {

            // Clear for test
            object = null;

            // Invoke function
            CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.function", null);
            assertSame(mockObject, object, "Should load the mock object");
        }
    }

    public static class MockObject {
    }

    public static class MockFunction {
        public void function(MockObject object) {
            CompileManagedObjectTest.object = object;
        }
    }

}
