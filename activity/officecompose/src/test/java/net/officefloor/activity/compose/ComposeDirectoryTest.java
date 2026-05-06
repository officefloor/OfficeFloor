package net.officefloor.activity.compose;

import lombok.Data;
import net.officefloor.activity.compose.build.ComposeArchitect;
import net.officefloor.activity.compose.build.ComposeContext;
import net.officefloor.activity.compose.build.ComposeEmployer;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.Closure;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ComposeDirectoryTest {

    @Test
    public void directory() throws Throwable {

        // Compile capturing the items
        Map<String, OfficeSection> items = new HashMap<>();
        Map<String, OfficeSection> naming = new HashMap<>();
        Closure<TestConfiguration> testConfiguration = new Closure<>();
        CompileOfficeFloor compiler = new CompileOfficeFloor();
        compiler.office((office) -> {
            ComposeArchitect architect = ComposeEmployer.employComposeArchitect(office.getOfficeArchitect(), office.getOfficeSourceContext());

            // Load all the compositions
            PropertyList properties = office.getOfficeSourceContext().createPropertyList();
            properties.addProperty("TestClass").setValue(this.getClass().getName());
            architect.addCompositions((itemContext, listener) -> {

                // Determine if configuration
                if ("configuration".equals(itemContext.getItemName())) {
                    // Just configuration
                    testConfiguration.value = itemContext.getConfiguration(TestConfiguration.class);

                } else {
                    // Create the composition
                    OfficeSection section = itemContext.addComposition(itemContext.getItemName(),
                            ComposeContext::getCompositionSection, ComposeConfiguration.class);

                    // Add the composition
                    naming.put(itemContext.getItemName(), section);
                    listener.composition(itemContext.getItemName(), section);
                }

            }, "directory", properties, items::put);
        });
        try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {
        }

        // Ensure have directory of items
        assertComposition("one", items, naming);
        assertComposition("{two}", items, naming);
        assertComposition("sub/three.GET", items, naming);
        assertComposition("sub/{four}", items, naming);
        assertComposition("sub/five", items, naming);
        assertComposition("sub/five/six", items, naming);
        assertEquals(6, items.size(), "Incorrect number of items");

        // Ensure can get configuration (without composition)
        assertNotNull(testConfiguration.value, "Should have configuration");
        assertEquals("value", testConfiguration.value.getTest(), "Incorrect configuration");
    }

    private static void assertComposition(String name, Map<String, OfficeSection> items, Map<String, OfficeSection> naming) {
        OfficeSection section = items.get(name);
        assertNotNull(section, "Should have section for name " + name);
        assertEquals(name, section.getOfficeSectionName(), "Incorrect section name");
        assertSame(section, naming.get(name), "Should have suggested name from file");
    }

    public static class DirectoryService {
        public void service() {
            // Nothing as confirming load of directory
        }
    }

    @Data
    public static class TestConfiguration {
        private String test;
    }

}
