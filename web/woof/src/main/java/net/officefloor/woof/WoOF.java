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

import net.officefloor.OfficeFloorMain;
import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.server.http.HttpServerLocation;

/**
 * <p>
 * Provides <code>main</code> method for running Web on OfficeFloor (WoOF)
 * application.
 * <p>
 * It also provides useful methods for testing the WoOF server.
 *
 * @author Daniel Sagenschneider
 */
public class WoOF extends OfficeFloorMain {

    /**
     * {@link System} {@link Property} name to configure list of profiles for
     * {@link WoOF}.
     */
    public static final String DEFAULT_OFFICE_PROFILES = WoofLoaderSettings.OFFICE_ALIAS_NAME + ".profiles";

    /**
     * Opens the WoOF application on the ports.
     *
     * @param httpPort           HTTP port.
     * @param httpsPort          HTTPS port.
     * @param propertyNameValues Additional property name/values.
     * @return {@link OfficeFloor} to the WoOF application.
     */
    public static OfficeFloor open(int httpPort, int httpsPort, String... propertyNameValues) {

        // Create properties
        String[] properties = new String[propertyNameValues.length + 4];
        properties[0] = HttpServerLocation.PROPERTY_HTTP_PORT;
        properties[1] = String.valueOf(httpPort);
        properties[2] = HttpServerLocation.PROPERTY_HTTPS_PORT;
        properties[3] = String.valueOf(httpsPort);
        System.arraycopy(propertyNameValues, 0, properties, 4, propertyNameValues.length);

        // Open the OfficeFloor
        return OfficeFloorMain.open(properties);
    }

}
