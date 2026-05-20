/*-
 * #%L
 * Web on OfficeFloor
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.woof;

import net.officefloor.compile.impl.ApplicationOfficeFloorSource;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.plugin.governance.clazz.Enforce;
import net.officefloor.plugin.governance.clazz.Govern;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.server.http.HttpClientTestUtil;
import net.officefloor.web.ObjectResponse;
import net.officefloor.web.build.HttpInput;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests composition configuration loading in WoOF.
 */
public class WoofCompositionTest {

    /**
     * Tests compose only activation, with no <code>application.woof</code> present.
     */
    @Test
    public void restLoaded() throws Exception {
        this.doTest("/", "\"REST\"", null);
    }

    public static class RestService {
        public void service(ObjectResponse<String> response) {
            response.send("REST");
        }
    }

    /**
     * Verifies the REST directory is overridden by the property.
     */
    @Test
    public void restCustomDirectory() throws Exception {
        this.doTest("/", "\"CUSTOM_REST\"", (context) -> {
            context.addOverrideProperty(WoofLoaderOfficeExtensionService.REST_DIRECTORY_PROPERTY, "officefloor/custom-rest");
        });
    }

    public static class CustomRestService {
        public void service(ObjectResponse<String> response) {
            response.send("CUSTOM_REST");
        }
    }

    /**
     * Ensure not register REST if disabled.
     */
    @Test
    public void notLoadWoof() throws Exception {
        this.doTest("/", WoofLoaderSettings.WoofLoaderConfigurerContext::notLoadWoof, (response) -> {
            assertEquals(404, response.getStatusLine().getStatusCode(), "REST should not be registered");
        });
    }

    /**
     * Verifies that composed managed objects are loaded and auto-wired into services.
     */
    @Test
    public void objectsLoaded() throws Exception {
        this.doTest("/", "\"INJECTED\"", WoofCompositionTest::serviceObjects);
    }

    public static class MockLoadedObject implements MockObject {
        public String getValue() {
            return "INJECTED";
        }
    }

    private static void serviceObjects(WoofLoaderSettings.WoofLoaderRunnableContext woofContext) {
        woofContext.notLoadWoof();
        woofContext.extend((context) -> {
            OfficeArchitect office = context.getOfficeArchitect();
            OfficeSection section = office.addOfficeSection("service",
                    ClassSectionSource.class.getName(), ObjectsService.class.getName());
            HttpInput input = context.getWebArchitect().getHttpInput(false, "GET", "/");
            office.link(input.getInput(), section.getOfficeSectionInput("service"));
        });
    }

    public static interface MockObject {
        String getValue();
    }

    public static class ObjectsService {
        public void service(MockObject mo, ObjectResponse<String> response) {
            response.send(mo.getValue());
        }
    }

    /**
     * Verifies the managed objects directory is overridden by the property.
     */
    @Test
    public void objectsCustomDirectory() throws Exception {
        this.doTest("/", "\"CUSTOM_INJECTED\"", (context) -> {
            context.addOverrideProperty(WoofLoaderOfficeExtensionService.OBJECTS_DIRECTORY_PROPERTY, "officefloor/custom-objects");
            serviceObjects(context);
        });
    }

    public static class MockCustomObject implements MockObject {
        public String getValue() {
            return "CUSTOM_INJECTED";
        }
    }

    /**
     * Ensure not load objects if disabled.
     */
    @Test
    public void notLoadObjects() throws Exception {
        this.doTest("/", "\"NOT_INJECTED\"", (context) -> {
            context.notLoadObjects();
            serviceObjects(context);

            // Manually add the objects
            context.extend((extension) -> {
                OfficeManagedObjectSource mos = extension.getOfficeArchitect()
                        .addOfficeManagedObjectSource("mock", ClassManagedObjectSource.class.getName());
                mos.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, MockNotLoadObject.class.getName());
                mos.addOfficeManagedObject("mock", ManagedObjectScope.THREAD);
            });
        });
    }

    public static class MockNotLoadObject implements MockObject {
        public String getValue() {
            return "NOT_INJECTED";
        }
    }

    /**
     * Verifies that composition governance files are loaded alongside REST.
     */
    @Test
    public void governanceLoaded() throws Exception {
        MockGovernance.managedObject = null;
        this.doTest("/governed", "\"Govern: INJECTED\"", null);
        assertNotNull(MockGovernance.managedObject, "Should govern the REST handling method");
    }

    public static class GovernanceService {
        public void service(MockObject managedObject, ObjectResponse<String> response) {
            response.send("Govern: " + managedObject.getValue());
        }
    }

    public static class MockGovernance {
        private static MockObject managedObject;
        @Govern
        public void govern(MockObject managedObject) {
            MockGovernance.managedObject = managedObject;
        }
        @Enforce
        public void enforce() {}
    }

    /**
     * Verifies the governance directory is overridden by the property.
     */
    @Test
    public void governanceCustomDirectory() throws Exception {
        MockCustomGovernance.managedObject = null;
        this.doTest("/governed", "\"Govern: INJECTED\"", (context) -> {
            context.addOverrideProperty(WoofLoaderOfficeExtensionService.GOVERN_DIRECTORY_PROPERTY, "officefloor/custom-govern");
        });
        assertNotNull(MockCustomGovernance.managedObject, "Should govern the REST handling method");
    }

    public static class MockCustomGovernance {
        private static MockObject managedObject;
        @Govern
        public void govern(MockObject managedObject) {
            MockCustomGovernance.managedObject = managedObject;
        }
        @Enforce
        public void enforce() {}
    }

    @Test
    public void httpSecurity() throws Exception {
        this.doTest("/secured", (context) -> {
        }, (response) -> {
            assertEquals(401, response.getStatusLine().getStatusCode(), "Should not be allowed access");
        });
    }

    public static class SecuredService {
        public void service() {
            fail("Should not be invoked, as no access");
        }
    }

    @Test
    public void detectWoofNotAvailableViaOverrideProperty() throws Exception {
        try (OfficeFloor officeFloor = WoofLoaderSettings.contextualLoad((context) -> {

            // Configure not loading WoOF as files missing
            context.setWoofPath("non-existent.woof");
            context.addOverrideProperty(WoofLoaderOfficeExtensionService.REST_DIRECTORY_PROPERTY, "rest/not/exist");

            // Open WoOF
            return WoOF.open();
        })) {
            for (String officeName : officeFloor.getOfficeNames()) {
                Office office = officeFloor.getOffice(officeName);
                assertEquals(0, office.getObjectNames().length, "Should be no objects registered");
                assertEquals(0, office.getFunctionNames().length, "Should be no functions registered");
            }
        }
    }

    @Test
    public void detectWoofNotAvailableViaOfficeProperty() throws Exception {
        try (OfficeFloor officeFloor = WoofLoaderSettings.contextualLoad((context) -> {

            // Configure not loading WoOF
            context.setWoofPath("non-existent.woof");

            // Open WoOF (configuring Office to not find composition files)
            return WoOF.open(
                    ApplicationOfficeFloorSource.OFFICE_NAME + "." + WoofLoaderOfficeExtensionService.REST_DIRECTORY_PROPERTY,
                    "rest/not/exist");
        })) {
            for (String officeName : officeFloor.getOfficeNames()) {
                Office office = officeFloor.getOffice(officeName);
                assertEquals(0, office.getObjectNames().length, "Should be no objects registered");
                assertEquals(0, office.getFunctionNames().length, "Should be no functions registered");
            }
        }
    }

    @FunctionalInterface
    protected static interface TestLogic {
        void test(WoofLoaderSettings.WoofLoaderRunnableContext context);
    }

    @FunctionalInterface
    protected static interface AssertResponse {
        void assertResponse(HttpResponse response) throws IOException;
    }

    protected void doTest(String path, String expectedResponse, TestLogic logic) throws Exception {
        this.doTest(path, logic, (response) -> {
            assertEquals(200, response.getStatusLine().getStatusCode(), "Should be successful request");
            assertEquals(expectedResponse, HttpClientTestUtil.entityToString(response), "Incorrect response");
        });
    }

    protected void doTest(String path, TestLogic logic, AssertResponse assertResponse) throws Exception {
        try (OfficeFloor officeFloor = WoofLoaderSettings.contextualLoad((context) -> {

            // Load properties
            context.addOverrideProperty("TestClass", WoofCompositionTest.class.getName());

            // Test logic (avoiding loading WoOF model)
            context.setWoofPath("non-existent.woof");
            if (logic != null) {
                logic.test(context);
            }

            // Open WoOF
            return WoOF.open();
        })) {
            // Undertake request
            try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient()) {
                HttpResponse response = client.execute(new HttpGet("http://localhost:7878" + path));
                assertResponse.assertResponse(response);
            }
        }
    }

}
